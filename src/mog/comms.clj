(ns mog.comms
  (:require [mog.config :refer [config]]
            [mog.util :as util]
            [mount.core :refer [defstate]]))

(defn make-comm [id config]
  (let [f (util/kw->fn id)]
    (f config)))

(defn inst-comm []
  (println ":: starting with config:" config)
  (println ":: building com:" (:comm config))
  (make-comm (:comm config) config))

(defn stop-comm [comm]
  (let [[in-chan out-chan stop-fn] comm]
    (stop-fn)))

(defstate comms
  :start (inst-comm)
  :stop (stop-comm comms))
