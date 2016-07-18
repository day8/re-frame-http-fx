(ns re-frame-http-fx.core-test
  (:require
    [ajax.core     :as ajax]
    [cljs.test     :refer-macros [is deftest async]]
    [clojure.set   :as set]
    [cljs.spec     :as s]
    [re-frame.core :as re-frame]
    [re-frame-http-fx.core]))

;;
(s/def ::api-result
  (s/keys :req-un [::type ::issues_url ::url ::public_repos])) ;; loose spec

;; ---- TESTS ------------------------------------------------------------------


(deftest ^:async test-get
  (let [seen-events     (atom #{})
        forwarder       (re-frame/add-post-event-callback
                          (fn [source_event _]
                            (swap! seen-events #(->> source_event first (conj %)))))]
    ;; setup effects handler
    (re-frame/def-event-fx
      ::http-test
      (fn [_context _event-v]
        {:http {:method          :get
                :uri             "https://api.github.com/orgs/day8"
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [::good-http-result "test-token1"]
                :on-failure      [::bad-http-result "test-token1"]}}))
    ;; setup success handler
    (re-frame/def-event
      ::good-http-result
      (fn [db [_ token result]]
        (is (= "test-token1" token) "expected: token to passed through")
        (is (map? result) "expected: result should be a map")
        (is (every? keyword? (keys result)) "all keys should be keywords as per :response-format")
        ;; check shape of result using loose Spec
        (is (not= (s/conform ::api-result result) :cljs.spec/invalid)
            (s/explain-str ::api-result result))
        db))
    ;; kick things off
    (re-frame/dispatch [::http-test])
    ;; give re-frame and http time to process
    (async done
       (js/setTimeout
         (fn []
           (is (@seen-events ::good-http-result) "::good-http-result should have been fired")
           (done)
           (re-frame/remove-post-event-callback forwarder))
         3000))))
