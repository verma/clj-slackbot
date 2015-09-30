(ns clj-slackbot.evaluator
  (:require [clojail.core :refer [sandbox]]
            [clojail.testers :refer [secure-tester-without-def blanket]])
  (:import java.io.StringWriter))

(def clj-slackbot-tester
  (conj secure-tester-without-def (blanket "clj-slackbot")))

(def sb (sandbox clj-slackbot-tester))

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
