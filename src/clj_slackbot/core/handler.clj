(ns clj-slackbot.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester-without-def blanket]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clj-http.client :as client])
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException)
  (:gen-class))

(def clj-slackbot-tester
  (conj secure-tester-without-def (blanket "clj-slackbot")))

(def sb (sandbox clj-slackbot-tester))

(def post-url
  (:post-url env))

(def command-token
  (:command-token env))

(defn post-to-slack
  ([s channel]
     (let [p (if channel {:channel channel} {})]
       (client/post post-url
                   {:content-type :json
                    :form-params (assoc p :text s)})))
  ([s]
     (post-to-slack s nil)))

(defn eval-expr
  "Evaluate the given string"
  [s]
  (try
    (with-open [out (StringWriter.)]
      (let [form (binding [*read-eval* false] (read-string s))
            result (sb form {#'*out* out})]
        {:status true
         :input s
         :form form
         :result result
         :output (.toString out)}))
    (catch Exception e
      {:status false
       :input s
       :result (.getMessage e)})))

(defn format-result [r]
  (if (:status r)
    (str "```"
         "=> " (:form r) "\n"
         (when-let [o (:output r)]
           o)
         (if (nil? (:result r))
           "nil"
           (:result r))
         "```")
    (str "```"
         "==> " (or (:form r) (:input r)) "\n"
         (or (:result r) "Unknown Error")
         "```")))

(defn eval-and-post [s channel]
  (-> s
      eval-expr
      format-result
      (post-to-slack channel)))

(defn handle-clj [params]
  (if-not (= (:token params) command-token)
    {:status 403 :body "Unauthorized"}
    (let [channel (condp = (:channel_name params)
                    "directmessage" (str "@" (:user_name params))
                    "privategroup" (:channel_id params)
                    :else (str "#" (:channel_name params)))]
      (eval-and-post (:text params) channel)
      {:status 200
       :body ""
       :headers {"Content-Type" "text/plain"}})))

(defroutes approutes
  (POST "/clj" req (handle-clj (:params req)))
  (route/not-found "Not Found"))

(def app (wrap-defaults approutes
                        api-defaults))

(defn -main [& args]
  (run-jetty app {:port (if-let [p (:port env)]
                          (Integer/parseInt p)
                          3000)}))
