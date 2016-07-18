(ns re-frame-http-fx.core
  (:require
    [goog.net.ErrorCode :as errors]
    [re-frame.core      :refer [def-fx dispatch console]]
    [ajax.core          :as ajax]))

;; I provide the :http effect handler leveraging cljs-ajax lib
;; see API docs https://github.com/JulianBirch/cljs-ajax
;; Note we use the ajax-request.
;;
;; Deviation from cljs-ajax options in http-spec
;; :handler       - not supported, see :on-success and :on-failure
;; :on-success    - event vector dispatched with result
;; :on-failure    - event vector dispatched with result
;;
;; NOTE: if you nee tokens or other values for your handlers,
;;       provide them in the on-success and on-failure event e.g.
;;       [:success-event "my-token"] your handler will get event-v
;;       [:success-event "my-token" result]


(defn ajax-handler
  "ajax-request only provides a single handler for success and errors"
  [on-success on-failure xhrio [success? response]]
  ; see http://docs.closure-library.googlecode.com/git/class_goog_net_XhrIo.html
  (if success?
    (on-success response)
    (let [details (merge
                    {:uri             (.getLastUri xhrio)
                     :last-method     (.-lastMethod_ xhrio)
                     :last-error      (.getLastError xhrio)
                     :last-error-code (.getLastErrorCode xhrio)
                     :debug-message   (-> xhrio .getLastErrorCode (errors/getDebugMessage))}
                    response)]
      (on-failure details))))



(defn spec->ajax-options
  [{:as   http-spec
    :keys [on-success on-failure api response-format]
    :or   {on-success      [:http-no-on-success]
           on-failure      [:http-no-on-failure]
           api             (new js/goog.net.XhrIo)
           response-format (ajax/detect-response-format)}}]
  ; wrap events in cljs-ajax callbacks
  (-> http-spec
      (assoc
        :api             api
        :response-format response-format
        :handler        (partial ajax-handler
                                 #(dispatch (conj on-success %))
                                 #(dispatch (conj on-failure %))
                                 api))
      (dissoc :on-success :on-failure)))


(def-fx
  :http
  (fn http-effect [http-spec]
    ;;TODO verify with Spec
    (cond
      (or (list? http-spec) (vector? http-spec))
      (doseq [each http-spec] (http-effect each))

      (map? http-spec)
      (-> http-spec spec->ajax-options ajax/ajax-request)

      :else
      (console :error "re-frame-http-fx: expected :http effect to be a list or vector or map, but got: " http-spec))))
