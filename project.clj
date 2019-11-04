(defproject day8.re-frame/http-fx "0.1.7-SNAPSHOT"
  :description "A re-frame effects handler for performing Ajax tasks"
  :url "https://github.com/day8/re-frame-http-fx.git"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library]]
                 [thheller/shadow-cljs "2.8.69" :scope "provided"]
                 [re-frame "0.10.9" :scope "provided"]
                 [cljs-ajax "0.8.0"]]

  :plugins [[lein-shadow "0.1.5"]
            [lein-ancient "0.6.15"]
            [lein-shell "0.5.0"]]

  :deploy-repositories [["releases" {:sign-releases false :url "https://clojars.org/repo"}]
                        ["snapshots" {:sign-releases false :url "https://clojars.org/repo"}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]]}}

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
