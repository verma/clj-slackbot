 (ns clj-slackbot.core
   (:require [clj-slackbot.config :as config]
             [clj-slackbot.evaluator :as evaluator]
             [clj-slackbot.comms.slack-rtm]
             [clojure.core.async :as async :refer [>! <! go go-loop]]
             [clj-slackbot.util :as util])
   (:import java.lang.Thread)
  (:gen-class))

 (defn make-comm [id config]
   (let [f (util/kw->fn id)]
     (f config)))

 (defn -main [& args]
  (let [config (config/read-config)
        inst-comm (fn []
                    (println ":: building com:" (:comm config))
                    (make-comm (:comm config) config))]
    (println ":: starting with config:" config)

    (go-loop [[in out stop] (inst-comm)]
      (println ":: waiting for input")
      (if-let [form (<! in)]
        (let [input (:input form)
              res (evaluator/eval-expr input)]
          (println ":: form >> " input)
          (println ":: => " res)
          (>! out (assoc form :evaluator/result res))
          (recur [in out stop]))

        ;; something wrong happened, re init
        (do
          (println ":: WARNING! The comms went down, going to restart.")
          (stop)
          (<! (async/timeout 3000))
          (inst-comm))))

    (.join (Thread/currentThread))))

