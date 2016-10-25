
[![GitHub license](https://img.shields.io/github/license/Day8/re-frame-http-fx.svg)](license.txt)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-http-fx/tree/develop.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-http-fx/tree/develop)

## HTTP Effects Handler For re-frame

This re-frame library contains an HTTP [Effect Handler](https://github.com/Day8/re-frame/tree/develop/docs).

Keyed `:http-xhrio`, it wraps the goog xhrio API of [cljs-ajax](https://github.com/JulianBirch/cljs-ajax).

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
(reg-event-fx                             ;; note the trailing -fx
  :handler-with-http                      ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]                    ;; the first param will be "world"
    {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {:method          :get
                  :uri             "https://api.github.com/orgs/day8"
                  :timeout         8000                                           ;; optional see API docs
                  :response-format (ajax/json-response-format {:keywords? true})  ;; optional see API docs
                  :on-success      [:good-http-result]
                  :on-failure      [:bad-http-result]}}))
```

Look at the `:http-xhrio` line above. This library defines the "effects handler"
which implements `:http-xhrio`.

The supplied value should be an options map as defined by the simple interface `ajax-request` [see: api docs](https://github.com/JulianBirch/cljs-ajax#ajax-request).
Except for `:on-success` and `:on-failure`.

Don't provide:

     :api     - the effects handler explicitly uses xhrio so it will be ignored.
     :handler - we substitute this with one that dispatches the :on-success & :on-failure

You can also pass a list or vector of these options maps where multiple HTTPs are required.

###Step 3. Handlers for :on-success and :on-failure

Provide normal re-frame handlers for :on-success and :on-failure. You event
handlers will get the result as the last arg of their event-v. Here is an
example written as another effect handler to put the result into db.

```clj
(reg-event-db
  :good-http-result
  (fn [db [_ result]]
    (assoc db :api-result result)}))
```

The result passed to your :on-failure is always a map with various xhrio details provided.
See the fn [ajax-handler](/src/day8.re-frame.http-fx.cljs) for details

###TIP:

If you need additional arguments or identifying tokens in your handler, then
include them in your `:on-success` and `:on-failure` event vector in Step 3. they
will be passed along. Actual `result` will always be the last value.

TODO:
- introduce the notion of `delay`.  Wait N ms before actioning. To assist with retries?
