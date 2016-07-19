(ns re-frame-http-fx.core-test
  (:require
    [ajax.core     :as ajax]
    [cljs.test     :refer-macros [is deftest async]]
    [cljs.spec     :as s]
    [re-frame.core :as re-frame]
    [re-frame-http-fx.core]))

;;
(s/def ::api-result
  (s/keys :req-un [::type ::issues_url ::url ::public_repos])) ;; loose spec

;; ---- TESTS ------------------------------------------------------------------


(deftest ajax-get
  ;; Setup handler which returns a :http effect specifying an ajax-request.
  ;; Note we specify optional :response-format to make sure our json result
  ;; has keywords for keys, and :timeout see the ajax-request API
  ;; for more details https://github.com/JulianBirch/cljs-ajax#ajax-request
  ;; We specify an :on-failure for completenes but we don't expect the request
  ;; to fail unless there is something wrong with your internet or github.
  (re-frame/def-event-fx
    ::http-test
    (fn [_context _event-v]
      {:http {:method          :get
              :uri             "https://api.github.com/orgs/day8"
              :timeout         5000
              :response-format (ajax/json-response-format {:keywords? true})
              :on-success      [::good-http-result "test-token1"]
              :on-failure      [::bad-http-result "test-token1"]}}))
    (async done

      ;; setup success handler
      (re-frame/def-event
        ::good-http-result
        (fn [db [_ token result]]
          (is (= "test-token1" token) "expected: token passed through")
          ;; check shape of result using loose Spec
          (is (not= (s/conform ::api-result result) :cljs.spec/invalid)
              (s/explain-str ::api-result result))
          (is (every? keyword? (keys result)) "keys should be keywords")
          (done)
          db))

      ;; setup failure handler
      (re-frame/def-event
        ::bad-http-result
        (fn [db [_ _token error]]
          (re-frame/console :error "Unexpected HTTP error, something wrong with your internet? " error)
          (done)
          db))

      ;; kick things off
      (re-frame/dispatch [::http-test])))
