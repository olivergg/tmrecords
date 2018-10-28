(ns tmrecords.views
  (:require
   [re-frame.core :as re-frame]
   [tmrecords.subs :as subs]))
   


;; score tablese

(defn score-tables []
  ;; TODO : read a subscrition to create the table
  [:div.scoreContainer
   [:table#scoreTable.scoreTable
    [:tbody
     [:tr
      [:th "Tracks \\ User"] [:th "Mathieu"] [:th "Aymeric"]]
     [:tr
      [:td "shorter 01"] [:td "12:03.99"] [:td "12:03.93"]]
     [:tr
      [:td "shorter 02"] [:td "12:12.12"] [:td "13:12.12"]]]]])


;; home
(defn home-panel []
  (let [lastupd (re-frame/subscribe [::subs/last-updated])]
    [:div
     [:section.header
      [:img
       {:width "150",
        :src "tm-main.png",
        :alt "tm-main.png"}]
      [:div.bigTitle "Records TrackMania"]]
     [score-tables]
     [:section.footer 
      [:a {:href "#/about"} "About"]
      [:a {:style {:margin-left "10px"}} (str "Last updated : " @lastupd)]]]))



;; about
(defn about-panel []
  [:div
   [:h1 "About"]
   [:div "Powered by re-frame, a React based Clojurescript framework"]
   [:div "Style and images credits to Aymeric Malchrowicz"]
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
