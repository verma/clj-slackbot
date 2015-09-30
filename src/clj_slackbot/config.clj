 (ns clj-slackbot.config
   (:require [clojure.edn :as edn]
             [environ.core :refer [env]]))


 (defn read-config []
   (let [path (or (:config-file env)
                  "config.edn")]
     (edn/read-string (slurp path))))
