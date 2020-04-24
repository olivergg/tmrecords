(ns tmrecords.core
  (:require
   [reagent.dom :as reagent]
   [re-frame.core :as re-frame]
   [tmrecords.events :as events]
   [tmrecords.routes :as routes]
   [tmrecords.views :as views]
   [tmrecords.firebase :as firebase]))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init "Entry point called from index.html" []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (firebase/init)
  (mount-root))
