(ns day8.re-frame.http-fx-test
  (:require
    [ajax.core     :as ajax]
    [cljs.test     :refer-macros [is deftest async use-fixtures]]
    [cljs.spec     :as s]
    [re-frame.core :as re-frame]
    [day8.re-frame.http-fx]))

;;
(s/def ::api-result
  (s/keys :req-un [::type ::issues_url ::url ::public_repos])) ;; loose spec

;; ---- FIXTURES ---------------------------------------------------------------
;; This fixture uses the re-frame.core/make-restore-fn to checkpoint and reset
;; to cleanup any dynamically registered handlers from our tests.
(defn fixture-re-frame
  []
  (let [restore-re-frame (atom nil)]
    {:before #(reset! restore-re-frame (re-frame.core/make-restore-fn))
     :after  #(@restore-re-frame)}))

(use-fixtures :each (fixture-re-frame))

;; ---- TESTS ------------------------------------------------------------------

;; setup success handler
(re-frame/reg-event-db
  ::good-http-result
  (fn [db [_ done token result]]
    (is (= "test-token1" token) "expected: token passed through")
    ;; check shape of result using loose Spec
    (is (not= (s/conform ::api-result result) :cljs.spec/invalid)
        (s/explain-str ::api-result result))
    (is (every? keyword? (keys result)) "keys should be keywords")
    (done)
    db))

(re-frame/reg-event-db
  ::good-post-result
  (fn [db [_ done data result]]
    ;; httpbin returns a JSON object containing headers and other
    ;; metadata from our request along with our JSON payload.
    (is (= (:json result) data))
    (done)
    db))

(re-frame/reg-event-db
  ::good-post-body-result
  (fn [db [_ done {:strs [form] :as result}]]
    (is (= (get form "username") "bob"))
    (is (= (get form "password") "sekrit"))
    (done)
    db))

;; setup failure handler
(re-frame/reg-event-db
  ::bad-http-result
  (fn [db [_ done token error]]
    (is (= "test-token1" token) "expected: token passed through")
    (cljs.test/do-report
      {:type     :fail
       :message  "Unexpected HTTP error, something wrong with your internet?"
       :expected ::good-http-result
       :actual   error})
    (done)
    db))

(re-frame/reg-event-fx
  ::http-test
  (fn [_world [_ val]]
    {:http-xhrio val}))

(deftest xhrio-get-test
  ;; Setup effects handler with :http-xhrio specifying an ajax-request.
  ;; Note we specify optional :response-format to make sure our json result
  ;; has keywords for keys, and :timeout see the ajax-request API
  ;; for more details https://github.com/JulianBirch/cljs-ajax#ajax-request
  ;; We specify an :on-failure for completeness but we don't expect the request
  ;; to fail unless there is something wrong with your internet or github.
  (async done
    (re-frame/dispatch [::http-test {:method          :get
                                     :uri             "https://api.github.com/orgs/day8"
                                     :timeout         5000
                                     :response-format (ajax/json-response-format {:keywords? true})
                                     :on-success      [::good-http-result done "test-token1"]
                                     :on-failure      [::bad-http-result done "test-token1"]}])))

(deftest xhrio-post-params-test
  (async done
    (let [data {:here ["is" "a" "map"]}]
      (re-frame/dispatch [::http-test {:method          :post
                                       :uri             "https://httpbin.org/post"
                                       :params          data
                                       :timeout         5000
                                       :format          (ajax/json-request-format)
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success      [::good-post-result done data]
                                       :on-failure      [::bad-http-result done "test-token1"]}]))))

(deftest xhrio-post-body-test
  (async done
    (re-frame/dispatch [::http-test {:method          :post
                                     :uri             "https://httpbin.org/post"
                                     :body            (doto (js/FormData.)
                                                        (.append "username" "bob")
                                                        (.append "password" "sekrit"))
                                     :timeout         5000
                                     :format          (ajax/json-request-format)
                                     :response-format (ajax/json-response-format)
                                     :on-success      [::good-post-body-result done]
                                     :on-failure      [::bad-http-result done "test-token1"]}])))

(deftest xhrio-get-seq-test
  ;; Setup effects handler with :http-xhrio specifying an ajax-request.
  ;; Note we specify optional :response-format to make sure our json result
  ;; has keywords for keys, and :timeout see the ajax-request API
  ;; for more details https://github.com/JulianBirch/cljs-ajax#ajax-request
  ;; We specify an :on-failure for completeness but we don't expect the request
  ;; to fail unless there is something wrong with your internet or github.
  (async done
    (re-frame/dispatch [::http-test [{:method          :get
                                      :uri             "https://api.github.com/orgs/day8"
                                      :timeout         5000
                                      :response-format (ajax/json-response-format {:keywords? true})
                                      :on-success      [::good-http-result done "test-token1"]
                                      :on-failure      [::bad-http-result done "test-token1"]}]])))

;(deftest invalid-fx-test
;  (is (= ::s/invalid
;         (s/conform ::http-fx/request-map {})))
;  (is (= ::s/invalid
;         (s/conform ::http-fx/request-map {:method          :get
;                                           :uri             "https://api.github.com"
;                                           :response-format :json
;                                           :on-success      [:x]
;                                           :on-failure      [:y]})))
;  (is (= ::s/invalid
;         (s/conform ::http-fx/request-map {:method          :get
;                                           :uri             "https://api.github.com"
;                                           :response-format (ajax/json-response-format)
;                                           :on-success      [:x]}))))
