(ns tmrecords.views
  (:require
   [re-frame.core :as rf]
   [tmrecords.subs :as subs]
   [goog.string :as gstring]
   goog.string.format))


;; helper functions
(defn readable-duration
  ;; format number of seconds to readable format mm:ss.SSS
  [seconds]
  (let [centisec (-> seconds (* 100) (Math/round) (int))
        cent-r (rem centisec 100)
        minutes-int (quot seconds 60)
        minutes-r (rem seconds 60)]
    (if (>= seconds 0)
      (gstring/format "%02d:%02d.%02d" minutes-int minutes-r cent-r)
      "-")))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; a score row renderer (output a tr element)
(defn record-row [r]
  (let [players @(rf/subscribe [::subs/get-players])
        trackname (:track r)
        times (:times r)
        podium (mapv first (take 3 (sort-by val times)))]

    [:tr {:key trackname} [:td trackname]
       (for [p players
             :let [position (.indexOf podium p)]]
         [:td {:key (str trackname p)

               :class-name (case position
                             0 "best"
                             1 "secondbest"
                             2 "thirdbest"
                             "")}

              (as-> times x
                    (get x p "-")
                    (readable-duration x))])]))






;; score tables
(defn score-tables []
  ;; a simple table that displays the records stored in the database
  (let [players @(rf/subscribe [::subs/get-players])
        records @(rf/subscribe [::subs/get-records])]
                                    
   [:div.scoreContainer
    [:table#scoreTable.scoreTable
     [:tbody
      (into [:tr [:th "Tracks"]] (for [p players] [:th p]))

      ;; TODO : separate function to render the rows (a row renderer)
      (doall (for [r records]
                (record-row r)))]]]))

;; footer
(defn footer[]
  (let [lastupd @(rf/subscribe [::subs/last-updated])
        user @(rf/subscribe [:user])
        connected (not (nil? (:display-name user)))]
   [:section.footer
    [:a (get user :display-name "Not connected")]
    (if connected
      [:a {:href "#" :on-click #(rf/dispatch [:sign-out])} "Sign out"]
      [:a {:href "#" :on-click #(rf/dispatch [:sign-in])} "Sign in"])
    [:a {:href "#/about"} "About"]
    [:a#lastupdatedlnk (str "Last updated : " lastupd)]]))

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
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
