> Status:  still under development. Don't use yet.

## Async HTTP In re-frame

Herein a re-frame effects handler, named `:http` takes a declarative specification of HTTP requests. Leverages the [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) library and emits on-success and on-failure re-frame events

## Quick Start Guide
 
### Step 1. Add Dependency
 
Add the following project dependency:  
[![Clojars Project](http://clojars.org/re-frame-http-fx/latest-version.svg)](http://clojars.org/re-frame-http-fx)


### Step 2. Registration

In your root namespace, called perhaps `core.cljs`, in the `ns`...

```clj
(ns app.core
  (:require 
    ...
    [re-frame-http-fx]   ;; <-- add this
    ...))
```
Because we never subsequently use this namespace, it 
appears redundant.  But the require will cause the `:http` effect handler to self-register with re-frame, which is important to everything that follows.

###Step 3. Event handler
In your event handlers namespace, perhaps called `events.cljs`...

Write the event handler which will use this effect :

```clj
(def-event-fx                    ;; note the fx
  :handler-with-http             ;; usage:  (dispatch [:handler-with-http])
  (fn [_ _]
    {:db   (.....)               ;; whatever effect on db you need if any
     :http {:method     :get
            :uri        "https://api.github.com/orgs/day8"
            :on-success [:good-http-result]
            :on-failure [:bad-http-result]}}))
```

Look at the :http line above. This library defines the "effect handler" which implements `:http`. It takes an options map. Apart from the `:on-success` and `:on-failure` the remaining args are as per the [cljs-ajax api docs](https://github.com/JulianBirch/cljs-ajax)

###Step 4. Handlers for :on-success and :on-failure

Define normal re-frame handlers for :on-success and :on-failure. You event handlers will get the result as the last arg of their event-v. Here is an example written as another effect handler to put the result into db.

```clj
(def-event-fx
  :good-http-result
  (fn [context [_ result]
    {:db   (assoc-in context [:db :http-result] result)}))  ;; apply whatever effect on db you need
```

The result passed to your :on-failure is always a map with various xhrio details provided. See the fn [ajax-handler](/src/re-frame-http-fx.core.cljs) for details
###TIP:
If you need additional arguments or identifying tokens in your handler, then include them in your `:on-success` and `:on-failure` event vector in Step 3. they will be passed along. Actual `result` will always be the last value.
    
