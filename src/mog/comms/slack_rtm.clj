(ns mog.comms.slack-rtm
  (:require [clojure.core.async :as async :refer [go go-loop]]
            [clj-http.client :as http]
            [gniazdo.core :as ws]
            [cheshire.core :refer [parse-string generate-string]]
            [mog.util :as util]))

(def ^:private rtm-socket-url
  "https://slack.com/api/rtm.start")

(defn get-websocket-url [api-token]
  (let [response (-> (http/get rtm-socket-url
                               {:query-params {:token      api-token
                                               :no_unreads true}
                                :as :json})
                     :body)]
    (when (:ok response)
      (:url response))))

(defn connect-socket [url]
  (let [in (async/chan)
        out (async/chan)
        socket (ws/connect
                 url
                 :on-receive
                 (fn [m]
                   (async/put! in (parse-string m true)))
                 :on-error
                 (fn [_]
                   (async/close! in)))]
    (go-loop []
      (let [m (async/<! out)
            s (generate-string m)]
        (ws/send-msg socket s)
        (recur)))
    [in out]))

(defn- can-handle? [data prefix]
  (when-let [text (:text data)]
    (.startsWith text prefix)))

(defn start-event-loop [{:keys [api-token prefix]}]
  (let [event-in (async/chan 10)
        event-out (async/chan 10)
        url (get-websocket-url api-token)
        counter (atom 0)
        next-id (fn []
                  (swap! counter inc))
        shutdown (fn []
                   (async/close! event-in)
                   (async/close! event-out))]
    (when (clojure.string/blank? url)
      (throw (ex-info "Could not get RTM Websocket URL" {})))

    (println ":: got websocket url:" url)

    ;; start a loop to process event messages
    (go-loop [[ws-in ws-out] (connect-socket url)]
      ;; get whatever needs to be done for either data coming from the socket
      ;; or from the user
      (let [[val port] (async/alts! [event-out ws-in])]
        ;; if something goes wrong, just die for now
        ;; we should do something smarter, may be try and reconnect
        (if (nil? val)
          (do
            (println ":: shutting down")
            (shutdown))
          (do
            (if (= port event-out)
              ;; send response to the remote end point via the
              ;; websocket connection
              (async/>! ws-out {:id      (next-id) :type "message"
                                :channel (get-in val [:meta :channel])
                                :text    (-> val
                                             util/format-result-for-slack)})

              ;; the websocket has sent us something, figure out if its of interest
              ;; to us, and if it is, send it to the bot's brain
              (do
                (println ":: incoming:" val)
                ;; When the input has the prefix, process it as a
                ;; command.
                (when (can-handle? val prefix)
                  (async/>! event-in {:input (subs (:text val) 1)
                                      :meta  val}))))
            (recur [ws-in ws-out])))))
    [event-in event-out shutdown]))
