(ns clj-slackbot.comms.slack-web-hook
  (:require [clj-slackbot.util :as util]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.core.async :as async :refer [>!! <!! go-loop]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn post-to-slack
  ([post-url s channel]
   (client/post post-url
     {:content-type :json
      :form-params  (cond-> {:text s}
                      channel (assoc :channel channel))
      :query-params {"parse" "none"}}))
  ([post-url s]
   (post-to-slack post-url s nil)))

(defn handle-clj
  [{:keys [token channel_id channel_name response_url text user_name]} command-token cin]
  (if-not (= token command-token)
    {:status 403 :body "Unauthorized"}
    (let [channel (case channel_name
                    "directmessage" (str "@" user_name)
                    "privategroup" channel_id
                    (str "#" channel_name))]
      ;; send the form to our evaluator and get out of here
      (>!! cin {:input text
                :meta  {:channel      channel
                        :response-url response_url
                        :user user_name}})

      {:status 200 :body "..." :headers {"Content-Type" "text/plain"}})))


(defn start
  [{:keys [port command-token]}]
  ;; check we have everything
  (when (some nil? [port command-token])
    (throw (Exception. "Cannot initialize. Missing port or command-token")))

  (println ":: starting http server on port:" port)
  (let [cin (async/chan 10)
        cout (async/chan 10)
        app (-> (routes
                  (POST "/clj" req (handle-clj (:params req)
                                               command-token
                                               cin))
                  (route/not-found "Not Found"))
                (wrap-defaults api-defaults))]
    ;; start the loops we need to read back eval responses
    (go-loop [{{post-url :response-url channel :channel} :meta :as res} (<!! cout)]
      (if-not res
        (println "The form output channel has been closed. Leaving listen loop.")
        (do
          (post-to-slack
            post-url
            (util/format-result-for-slack res)
            channel)
          (recur (<!! cout)))))

    ;; start web listener
    (let [server (run-server app {:port port})]
      [cin cout (fn []
                  (async/close! cin)
                  (async/close! cout)
                  (server))])))


