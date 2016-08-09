(ns mog.config
  (:require [clojure.edn :as edn]
            [environ.core :refer [env]]
            [mount.core :refer [defstate]]))

(defn read-config []
  (let [path (or (:config-file env)
                 "config.edn")]
    (edn/read-string (slurp path))))

(defstate config
  :start (read-config))
