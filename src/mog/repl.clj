(ns mog.repl
  (:require [clojure.tools.namespace.repl :as tn]
            [mog.comms :refer [comms]]
            [mog.core :refer [main]]
            [mount.core :as mount]))

(defn go []
  (mount/start)
  :ready)

(defn stop []
  (mount/stop))

(defn reset []
  (mount/stop)
  (tn/refresh :after 'mog.repl/go))
