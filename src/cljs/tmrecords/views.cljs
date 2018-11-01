(ns tmrecords.views
  (:require
   [re-frame.core :as re-frame]
   [tmrecords.subs :as subs]
   [goog.string :as gstring]
   goog.string.format))


;; helper functions
(defn readable-duration
  ;; format number of seconds to readable format mm:ss.SSS
  [seconds]
  (let [centisec (-> seconds (* 100) (Math/ceil) (int))
        cent-r (rem centisec 100)
        minutes-int (quot seconds 60)
        minutes-r (rem seconds 60)]
    (if (>= seconds 0)
      (gstring/format "%02d:%02d.%02d" minutes-int minutes-r cent-r)
      "-")))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; score tables
(defn score-tables []
  ;; a simple table that displays the records stored in the database
  (let [players @(re-frame/subscribe [::subs/get-players])
        records @(re-frame/subscribe [::subs/get-records])]
                                    
   [:div.scoreContainer
    [:table#scoreTable.scoreTable
     [:tbody
      (into [:tr [:th "Tracks"]] (for [p players] [:th p]))

      ;; TODO : separate function to render the rows (a row renderer)
      (for [r records]
        [:tr {:key (:track r)} [:td (:track r)]
           (for [p players]
             [:td {:key (str (:track r) p)}
              (as-> (:times r) x
                              (get x p "-")
                              (readable-duration x))])])]]]))

;; footer
(defn footer[]
  (let [lastupd (re-frame/subscribe [::subs/last-updated])
        user (re-frame/subscribe [:user])]
   [:section.footer
    [:a (get @user :display-name "Not connected")]
    [:a {:href "#" :on-click #(re-frame/dispatch [:sign-in])} "Sign in"]
    [:a {:href "#" :on-click #(re-frame/dispatch [:sign-out])} "Sign out"]
    [:a {:href "#/about"} "About"]
    [:a {:style {:float "right" }} (str "Last updated : " @lastupd)]]))

;; home
(defn home-panel []
  [:div
   [:section.header
    [:img
     {:width "150",
      :src "tm-main.png",
      :alt "tm-main.png"}]
    [:div.bigTitle "Records TrackMania"]]
   [score-tables]
   [footer]])


    



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
