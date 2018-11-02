(ns tmrecords.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [tmrecords.events :as events]
   [tmrecords.routes :as routes]
   [tmrecords.views :as views]
   [tmrecords.config :as config]
   [tmrecords.firebase :as firebase]))



(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (firebase/init)
  (mount-root))
