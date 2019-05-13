
[![GitHub license](https://img.shields.io/github/license/Day8/re-frame-http-fx.svg)](license.txt)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master)

## HTTP Effects Handler For re-frame

This re-frame library contains an HTTP [Effect Handler](https://github.com/Day8/re-frame/tree/develop/docs).

Keyed `:http-xhrio`, it wraps the goog xhrio API of [cljs-ajax](https://github.com/JulianBirch/cljs-ajax).

> **IMPORTANT**: This effect handler depends entirely on the API of [cljs-ajax](https://github.com/JulianBirch/cljs-ajax). Make sure you are familiar with the API for cljs-ajax, and especially with [`ajax-request`](https://github.com/JulianBirch/cljs-ajax#ajax-request) before proceeding.

## Quick Start Guide

### Step 1. Add Dependency

Add the following project dependency: <br>
[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/http-fx.svg)](https://clojars.org/day8.re-frame/http-fx)

Requires re-frame >= 0.8.0

### Step 2. Registration And Use

In the namespace where you register your event handlers, perhaps called `events.cljs`, you have 2 things to do.

**First**, add this "require" to the `ns`:
```clj
(ns app.core
  (:require
    ...
    [day8.re-frame.http-fx]   ;; <-- add this
    ...))
```

Because we never subsequently use this `require`, it
appears redundant.  But its existence will cause the `:http-xhrio` effect
handler to self-register with re-frame, which is important
to everything that follows.

**Second**, write a an event handler which uses this effect:
```clj
(ns app.events              ;; or where ever you define your event handlers
  (:require
    ...
    [ajax.core :as ajax]    ;; so you can use this in the response-format below
    ...))
    
(reg-event-fx                             ;; note the trailing -fx
  :handler-with-http                      ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]                    ;; the first param will be "world"
    {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {:method          :get
                  :uri             "https://api.github.com/orgs/day8"
                  :timeout         8000                                           ;; optional see API docs
                  :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                  :on-success      [:good-http-result]
                  :on-failure      [:bad-http-result]}}))
```

Look at the `:http-xhrio` line above. This library defines the "effects handler"
which implements `:http-xhrio`.

The supplied value should be an options map as defined by the simple interface `ajax-request` [see: api docs](https://github.com/JulianBirch/cljs-ajax#ajax-request). Except for `:on-success` and `:on-failure`. All options supported by `ajax-request`
should be supported by this library, as it is a thin wrapper over `ajax-request`.

Here is an example of a POST request. Note that `:format` also needs to be specified (unless you pass `:body` in the 
map).

```cljs
(re-frame/reg-event-fx
  ::http-post
  (fn [_world [_ val]]
    {:http-xhrio {:method          :post
                  :uri             "https://httpbin.org/post"
                  :params          data
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::success-post-result]
                  :on-failure      [::failure-post-result]}}))
```

**N.B.**: `ajax-request` is harder to use than the `GET` and `POST` functions
 cljs-ajax provides, but this gives you smaller code sizes from dead code elimination.
 **In particular, you MUST provide a `:response-format`, it is not inferred for you.**

Don't provide:

     :api     - the effects handler explicitly uses xhrio so it will be ignored.
     :handler - we substitute this with one that dispatches `:on-success` or `:on-failure` events.

You can also pass a list or vector of these options maps where multiple HTTPs are required.

To make **multiple requests**, supply a vector of options maps:
```
{:http-xhrio [ {...}
               {...}]}
```

### Step 3a. Handling `:on-success`

Provide normal re-frame handlers for `:on-success` and `:on-failure`. Your event
handlers will get the result as the last argument of their event vector. Here is an
example written as another effect handler to put the result into db.

```clojure
(reg-event-db
  ::success-http-result
  (fn [db [_ result]]
    (assoc db :success-http-result result)))
```


### Step 3b. Handling `:on-failure`

The `result` supplied to your `:on-failure` handler will be a map containing various xhrio details (details below). 
See the fn [ajax-xhrio-handler](/src/day8/re_frame/http_fx.cljs#L23) for details

#### Step 3.1 :on-failure result

A simple failure handler could be written this way ...

```clojure
(reg-event-db
  ::failure-http-result
  (fn [db [_ result]]
    ;; result is a map containing details of the failure
    (assoc db :failure-http-result result)))
```

##### status of 40x/50x

If the network connection to the server is successful, but the server returns an
error (40x/50x) HTTP status code `result` will be a map like:

```clojure
{:uri "/error"
 :last-method "GET"
 :last-error "Service Unavailable [503]"
 :last-error-code 6
 :debug-message "Http response at 400 or 500 level"
 :status 503
 :status-text "Service Unavailable"
 :failure :error
 :response nil}
```

##### Status 0

In some cases, if the network connection itself is unsuccessful, it is possible
to get a status code of `0`. For example:

- cross-site scripting whereby access is denied; or
- requesting a URI that is unreachable (typo, DNS issues, invalid hostname etc); or
- request is interrupted after being sent (browser refresh or navigates away from the page); or
- request is otherwise intercepted (check your ad blocker).

In this case, `result` will be something like:

```clojure
{:uri "http://i-do-not-exist/error"
 :last-method "GET"
 :last-error " [0]"
 :last-error-code 6
 :debug-message "Http response at 400 or 500 level"
 :status 0
 :status-text "Request failed."
 :failure :failed}
```

##### Status -1

If the time for the sever to respond exceeds `:timeout` `result` will be a map something 
like:

```clojure
{:uri "/timeout"
 :last-method "GET"
 :last-error "Timed out after 1ms, aborting"
 :last-error-code 8
 :debug-message "Request timed out"
 :status -1
 :status-text "Request timed out."
 :failure :timeout}
```

### Tip

If you need additional arguments or identifying tokens in your handler, then
include them in your `:on-success` and `:on-failure` event vector in Step 3. 

For example ... 

```cljs
(re-frame/reg-event-fx
  ::http-post
  (fn [_ [_ val]]
    {:http-xhrio {:method          :post
                  ...
                  :on-success      [::success-post-result 42 "other"]
                  :on-failure      [::failure-post-result :something :else]}}))
```

Notice the way that additional values are encoded into the success and failure event vectors. 

These event vectors will be dispatched (`result` is `conj`-ed to the end) making all encoded values AND the `result` available to the handlers. 
