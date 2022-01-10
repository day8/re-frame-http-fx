(defproject    day8.re-frame/http-fx "lein-git-inject/version"
  :description "A re-frame effects handler for performing Ajax tasks"
  :url         "https://github.com/day8/re-frame-http-fx.git"
  :license     {:name "MIT"}

  :dependencies [[org.clojure/clojure       "1.10.2"   :scope "provided"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs      "2.11.18"  :scope "provided"]
                 [re-frame                  "1.2.0"    :scope "provided"]
                 [cljs-ajax                 "0.8.4"]]

  :plugins      [[day8/lein-git-inject "0.0.14"]
                 [lein-shadow          "0.3.1"]
                 [lein-ancient         "0.6.15"]
                 [lein-shell           "0.5.0"]]

  :middleware   [leiningen.git-inject/middleware]

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_TOKEN}]]
  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.2"]]}}

  :clean-targets [:target-path
                  "node_modules"
                  "run/compiled"]

  :jvm-opts ^:replace ["-Xms256m" "-Xmx2g"]

  :source-paths ["src"]

  :test-paths ["test"]

  :resource-paths ["run/resources"]

  :shadow-cljs {:nrepl  {:port 8777}

                :builds {:build-report
                         {:target           :browser
                          :compiler-options {:language-in :es6
                                             :language-out :es6}
                          :release          {:modules {:http-fx {:entries [day8.re-frame.http-fx]}}}}
                         
                         :browser-test
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

  :aliases {"watch" ["do"
                     ["clean"]
                     ["shadow" "watch" "browser-test" "karma-test"]]

            "build-report" ["do"
                            ["clean"]
                            ["shadow" "run" "shadow.cljs.build-report" "build-report" "target/build-report.html"]]

            "ci"    ["do"
                     ["clean"]
                     ["shadow" "compile" "karma-test"]
                     ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]})
