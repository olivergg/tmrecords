(defproject tmrecords "0.1.1-SNAPSHOT"

  :dependencies [[org.clojure/clojure        "1.10.1"]
                 [org.clojure/clojurescript  "1.10.741"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [binaryage/devtools         "1.0.0"]
                 [thheller/shadow-cljs       "2.8.107"]
                 [thheller/shadow-cljsjs     "0.0.21"]
                 [reagent                    "0.10.0"]
                 [re-frame                   "0.12.0"]
                 [org.clojure/core.async     "1.1.587"]
                 [clj-commons/secretary      "1.2.4"]
                 [com.degel/re-frame-firebase "0.8.0"]
                 [day8.re-frame/tracing      "0.5.3"]]

  :source-paths ["src/clj" "src/cljs"]
  :aliases {"dev-auto" ["shadow" "watch" "app"]})
