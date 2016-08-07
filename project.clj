(defproject mog-clj-slackbot "0.1.1-SNAPSHOT"
  :description "A slackbot for managing our grocery list"
  :url "https://github.com/kliph/mog-clj-slackbot"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [compojure "1.4.0"]
                 [clojail "1.0.6"]
                 [clj-http "1.0.1"]
                 [cheshire "5.3.1"]
                 [environ "1.0.0"]
                 [stylefruits/gniazdo "0.4.1"]
                 [ring/ring-defaults "0.1.5"]
                 [http-kit "2.1.18"]]
  :plugins [[lein-ring "0.8.13"]]
  :uberjar-name "mog.jar"
  :main mog.core
  :profiles
  {:dev {:repl-options {:init-ns mog.core.handler}
         :dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}
   :uberjar {:aot :all}})
