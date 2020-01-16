(defproject    day8.re-frame/http-fx "lein-git-inject/version"
  :description "A re-frame effects handler for performing Ajax tasks"
  :url         "https://github.com/day8/re-frame-http-fx.git"
  :license     {:name "MIT"}

  :dependencies [[org.clojure/clojure       "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs      "2.8.83" :scope "provided"]
                 [re-frame                  "0.10.9" :scope "provided"]
                 [cljs-ajax                 "0.8.0"]]

  :plugins      [[day8/lein-git-inject "0.0.11"]
                 [lein-shadow          "0.1.7"]
                 [lein-shell           "0.5.0"]]

  :middleware   [leiningen.git-inject/middleware]

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_PASSWORD}]]
  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.11"]]}}

  :clean-targets [:target-path
                  "run/compiled"]

  :jvm-opts ^:replace ["-Xms256m" "-Xmx2g"]

  :source-paths ["src"]

  :test-paths ["test"]

  :resource-paths ["run/resources"]

  :shadow-cljs {:nrepl  {:port 8777}

                :builds {:browser-test
                         {:target           :browser-test
                          :ns-regexp        "day8.*-test$"
                          :test-dir         "run/resources/compiled_test"
                          :compiler-options {:pretty-print    true
                                             :external-config {:devtools/config {:features-to-install [:formatters :hints]}}}}
                         :karma-test
                         {:target           :karma
                          :ns-regexp        "day8.*-test$"
                          :output-to        "target/karma-test.js"
                          :compiler-options {:pretty-print true}}}}

  :aliases {"test-auto"  ["do"
                          ["clean"]
                          ["shadow" "watch" "browser-test"]]
            "karma-once" ["do"
                          ["clean"]
                          ["shadow" "compile" "karma-test"]
                          ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]})
