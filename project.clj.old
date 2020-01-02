(defproject tmrecords "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.6"]
                 [org.clojure/core.async "0.2.395"]
                 [com.degel/re-frame-firebase "0.7.0"]
                 [secretary "1.2.3"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [figwheel-sidecar "0.5.16"]
                   [cider/piggieback "0.3.5"]
                   [re-frisk "0.5.3"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   ]
                     

    :plugins      [[lein-figwheel "0.5.16"]]}
   :prod {}}
   

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "tmrecords.core/mount-root"}
     :compiler     {:main                 tmrecords.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           re-frisk.preload
                                           day8.re-frame-10x.preload
                                           ]
                    :external-config      {:devtools/config {:features-to-install :all}}}}
                    

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            tmrecords.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})


    
  
