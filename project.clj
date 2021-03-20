(defproject net.dhleong/spade "1.1.0-SNAPSHOT"
  :description "A nice tool to use in the Garden"
  :url "https://github.com/dhleong/spade"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [garden "1.3.10"]]

  :plugins [[lein-figwheel "0.5.19"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :doo {:paths {:karma "./node_modules/karma/bin/karma"}}

  :source-paths ["src"]
  :test-paths ["test"]

  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]

  :jar-exclusions [#"(?:^|\/)public\/"]

  :aliases {"test" ["do" ; "test"
                    ["doo" "chrome-headless" "test" "once"]]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "dev"]

                ;; The presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "spade.demo/mount-root"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and compiled your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls ["http://localhost:3449"]}

                :compiler {:main spade.demo
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/spade.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}

               ;; This next build is a compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/spade.js"
                           :main spade.core
                           :optimizations :advanced
                           :pretty-print false}}

               {:id "test"
                :source-paths ["src" "dev" "test"]
                :compiler {:main          spade.runner
                           :output-to     "resources/public/js/compiled/test.js"
                           :output-dir    "resources/public/js/compiled/test/out"

                           ; npm is only needed for installing test dependencies
                           :npm-deps {:karma "4.1.0"
                                      :karma-cljs-test "0.1.0"
                                      :karma-chrome-launcher "2.2.0"}
                           :install-deps true

                           :optimizations :none}}]}

  :figwheel {:css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             :nrepl-middleware
             [cider.piggieback/wrap-cljs-repl cider.nrepl/cider-middleware]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.19"]
                                  [cider/piggieback "0.4.1"]
                                  [reagent "0.8.1"]]

                   :plugins [[lein-doo "0.1.10"]]

                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
