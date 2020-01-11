(defproject tmrecords "0.1.1-SNAPSHOT"

  :dependencies [[org.clojure/clojure        "1.10.1"]
                 [org.clojure/clojurescript  "1.10.597"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs       "2.8.83"]
                 [thheller/shadow-cljsjs     "0.0.21"]
                 [reagent                    "0.9.0-rc2"]
                 [re-frame                   "0.11.0-rc2"]
                 [binaryage/devtools         "0.9.10"]
                 [org.clojure/core.async     "0.6.532"]
                 [com.degel/re-frame-firebase "0.8.0"]
                 [clj-commons/secretary      "1.2.4"]
                 [day8.re-frame/tracing      "0.5.3"]]

  :source-paths ["src/clj" "src/cljs"]

  :shadow-cljs {:nrepl {:port 8777}
                
                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :modules {:app {:init-fn tmrecords.core/init}}
                               :devtools {:http-root "resources/public"
                                          :http-port 8280}}}}

  :aliases {"dev-auto" ["shadow" "watch" "app"]})
