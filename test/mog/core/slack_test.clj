(ns mog.core.slack-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer [deftest testing is]]
            [mog.core :as core]))

(defn mock-command-loop []
  (let [in (async/chan)
        out (async/chan)
        stop (fn []
               (async/close! in)
               (async/close! out))]
    (core/command-loop [in out stop])
    [in out stop]))

(deftest command-loop
  (testing "pass through"
    (let [[in-chan out-chan stop-fn] (mock-command-loop)
          input {:input "test"}
          output (do (async/>!! in-chan input)
                     (async/<!! out-chan))]
      (is (= (:input input) (:mog/response output))))))
