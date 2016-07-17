> Status:  still under development. Don't use yet.

[![Clojars Project](https://img.shields.io/clojars/v/re-frame-http-fx/latest-version.svg)](https://clojars.org/re-frame-http-fx)
[![GitHub license](https://img.shields.io/github/license/Day8/re-frame-http-fx.svg)](license.txt)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-http-fx/tree/master)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-http-fx/tree/develop.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-http-fx/tree/develop)

## HTTP Effects Handler For re-frame

Herein a re-frame ["effects handler"](https://github.com/Day8/re-frame/wiki/Effectful-Event-Handlers), 
keyed `:http`, which leverages [cljs-ajax](https://github.com/JulianBirch/cljs-ajax). 

## Quick Start Guide
 
### Step 1. Add Dependency
 
Add the following project dependency:  
[![Clojars Project](https://img.shields.io/clojars/v/re-frame-http-fx/latest-version.svg)](https://clojars.org/re-frame-http-fx)


### Step 2. Registration And Use

In the namespace where you register your event handlers, perhaps called `events.cljs`, you have 2 things to do.

**First**, add this "require" to the `ns`:
```clj
(ns app.core
  (:require 
    ...
    [re-frame-http-fx]   ;; <-- add this
    ...))
```

Because we never subsequently use this `require`, it 
appears redundant.  But its existence will cause the `:http` effect 
handler to self-register with re-frame, which is important
to everything that follows.

**Second**, write a an event handler which uses this effect:
```clj
(def-event-fx                    ;; note the trailing -fx
  :some-handler-with-http        ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]           ;; the first param will be "world" 
    {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
     :http {:method     :get
            :uri        "https://api.github.com/orgs/day8"
            :on-success [:good-http-result]
            :on-failure [:bad-http-result]}}))
```

Look at the `:http` line above. This library defines the "effect handler" 
which implements `:http`. 

The value supplied should be an options map as per [cljs-ajax api docs](https://github.com/JulianBirch/cljs-ajax). 
Except for `:on-success` and `:on-failure`.

###Step 3. Handlers for :on-success and :on-failure

Provide normal re-frame handlers for :on-success and :on-failure. You event 
handlers will get the result as the last arg of their event-v. Here is an 
example written as another effect handler to put the result into db.

```clj
(def-event
  :good-http-result
  (fn [db [_ result]
    (assoc db :api-result result)}))
```

The result passed to your :on-failure is always a map with various xhrio details provided. 
See the fn [ajax-handler](/src/re-frame-http-fx.core.cljs) for details

###TIP:
If you need additional arguments or identifying tokens in your handler, then 
include them in your `:on-success` and `:on-failure` event vector in Step 3. they 
will be passed along. Actual `result` will always be the last value.
    
TODO:

XXX value can be a `list` of maps where multiple HTTPs required
XXX Are we using the `ajax-request` API?  If so, I wonder why?
