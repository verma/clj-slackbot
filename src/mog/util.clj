(ns mog.util)

(defn format-result-for-slack [r]
  (str r))


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
