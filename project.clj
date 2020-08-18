(defproject    day8.re-frame/http-fx "lein-git-inject/version"
  :description "A re-frame effects handler for performing Ajax tasks"
  :url         "https://github.com/day8/re-frame-http-fx.git"
  :license     {:name "MIT"}

  :dependencies [[org.clojure/clojure       "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs      "2.10.19" :scope "provided"]
                 [re-frame                  "1.0.0" :scope "provided"]
                 [cljs-ajax                 "0.8.0"]]

  :plugins      [[day8/lein-git-inject "0.0.14"]
                 [lein-shadow          "0.2.2"]
                 [lein-shell           "0.5.0"]]

  :middleware   [leiningen.git-inject/middleware]

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_TOKEN}]]
  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.2"]]}}

  :clean-targets [:target-path
                  "package.json"
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

  ;; The git update-index command is required to ignore changes to package.json as
  ;; 1. package.json must be committed to the repo for npm install --save... to behave correctly, which is used by
  ;;    lein-shadow to install dependencies that would cause the build to fail if missing; e.g. karma
  ;; 2. .gitignore does nothing for files that are already committed
  ;; 3. git recognising package.json modifications would cause day8/lein-git-inject to always incorrectly use
  ;;    -SNAPSHOT versions.
  :aliases {"test-auto"  ["do"
                          ["clean"]
                          ["shell" "git" "update-index" "--assume-unchanged" "package.json"]
                          ["shadow" "watch" "browser-test"]]
            "karma-once" ["do"
                          ["clean"]
                          ["shell" "git" "update-index" "--assume-unchanged" "package.json"]
                          ["shadow" "compile" "karma-test"]
                          ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]})
