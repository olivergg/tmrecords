(ns tmrecords.views
  (:require
   [re-frame.core :as re-frame]
   [tmrecords.subs :as subs]
   ))


;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/last-updated])]
    [:div
     [:section.header
      [:img
       {:width "150",
        :src "tm-main.png",
        :alt "tm-main.png"}]
      [:div.bigTitle "Records TrackMania"]]
     [:section.footer
      [:a {:href "#/about"}
       "About"]
      [:a (str "Dernière mise à jour " @name) ]]]
    ))


;; about

(defn about-panel []
  [:div
   [:h1 "Powered by the powerful re-frame clojurescript framework."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
