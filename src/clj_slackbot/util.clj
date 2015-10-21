(ns clj-slackbot.util)

(defn format-result-for-slack [r]
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


(defn safe-resolve [kw]
  (let [user-ns (symbol (namespace kw))
        user-fn (symbol (name kw))]
      (try
        (ns-resolve user-ns user-fn)
        (catch Exception e
          (require user-ns)
          (ns-resolve user-ns user-fn)))))


(defn kw->fn [kw]
  (try
    (safe-resolve kw)
    (catch Throwable e
      (throw (ex-info "Could not resolve symbol on classpath" {:kw kw})))))