(defproject mog-clj-slackbot "0.1.1-SNAPSHOT"
  :description "A slackbot for managing our grocery list"
  :url "https://github.com/kliph/mog-clj-slackbot"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [org.clojure/core.async "0.2.385"]
                 [compojure "1.5.1"]
                 [clojail "1.0.6"]
                 [clj-http "2.2.0"]
                 [cheshire "5.6.3"]
                 [environ "1.1.0"]
                 [stylefruits/gniazdo "1.0.0"]
                 [ring/ring-defaults "0.2.1"]
                 [http-kit "2.1.18"]]
  :plugins [[lein-ring "0.9.7"]]
  :uberjar-name "mog.jar"
  :main mog.core
  :profiles
  {:dev {:repl-options {:init-ns mog.core.handler}}
   :uberjar {:aot :all}})
