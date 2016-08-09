(ns mog.core
  (:require [clojure.core.async :as async]
            [mog.comms :refer [comms]]
            [mog.config :refer [config]]
            [mog.util :as util]
            [mount.core :refer [defstate] :as mount])
  (:import java.lang.Thread)
  (:gen-class))

(defn command-loop [comms]
  (async/go-loop [[in out stop] comms]
    (println ":: waiting for input")
    (if-let [event (async/<! in)]
      (let [input (:input event)
            res input]
        (println ":: event >> " input)
        (println ":: => " res)
        (async/>! out (assoc event :mog/response res))
        (recur [in out stop])))))

(defstate main
  :start (command-loop comms))

(defn -main [& args]
  (println ":: starting with config:" config)
  (mount/start)
  (.join (Thread/currentThread)))
