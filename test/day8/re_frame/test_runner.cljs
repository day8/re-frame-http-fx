(ns day8.re-frame.http-fx.test-runner
  (:require
    [cljs.test         :as cljs-test :include-macros true]
    [jx.reporter.karma :as karma :include-macros true]
    [devtools.core     :as devtools]
    ;; Test Namespaces -------------------------------
    [day8.re-frame.http-fx-test])
  (:refer-clojure :exclude (set-print-fn!)))

(enable-console-print!)
(devtools/install! [:custom-formatters :sanity-hints]) ;; we love https://github.com/binaryage/cljs-devtools

;; ---- BROWSER based tests ----------------------------------------------------
(defn ^:export set-print-fn! [f]
  (set! cljs.core.*print-fn* f))


(defn ^:export run-html-tests []
  (cljs-test/run-all-tests #"day8.re-frame.http-fx.*-test"))

;; ---- KARMA  -----------------------------------------------------------------

(defn ^:export run-karma [karma]
  (karma/run-tests
    karma
    'day8.re-frame.http-fx-test))
