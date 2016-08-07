(ns mog.comms.slack-web-hook
  (:require [mog.util :as util]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as client]
            [clojure.core.async :as async :refer [>!! <!! go-loop]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn post-to-slack
  ([post-url s channel]
     (let [p (if channel {:channel channel} {})]
       (client/post post-url
                   {:content-type :json
                    :form-params (assoc p :text s)
                    :query-params {"parse" "none"}})))
  ([post-url s]
     (post-to-slack post-url s nil)))

(defn handle-clj [params command-token cin]
  (if-not (= (:token params) command-token)
    {:status 403 :body "Unauthorized"}
    (let [channel (condp = (:channel_name params)
                    "directmessage" (str "@" (:user_name params))
                    "privategroup" (:channel_id params)
                    (str "#" (:channel_name params)))]
      ;; send the form to our brain and get out of here
      (>!! cin {:input (:text params)
                :meta {:channel channel}})

      {:status 200 :body "..." :headers {"Content-Type" "text/plain"}})))


(defn start [{:keys [port post-url command-token] :as config}]
  ;; check we have everything
  (when (some nil? [port post-url command-token])
    (throw (Exception. "Cannot initialize. Missing port, post-url or command-token")))

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
    (go-loop [res (<!! cout)]
      (if-not res
        (println "The form output channel has been closed. Leaving listen loop.")
        (let [result (:mog/result res)
              channel (get-in res [:meta :channel])]
          (post-to-slack
            post-url
            (util/format-result-for-slack result)
            channel)
          (recur (<!! cout)))))

    ;; start web listener
    (let [server (run-server app {:port port})]
      [cin cout (fn []
                  (async/close! cin)
                  (async/close! cout)
                  (server))])))
