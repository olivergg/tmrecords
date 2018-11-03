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

;; green header :-)
(defn header []
  [:div.header-wrapper [:section.header
                        [:img
                         {:width "150",
                          :src "tm-main.png",
                          :alt "tm-main.png"}]
                        [:div.bigTitle "Records TrackMania"]]])


;; olympic ranking table
(defn ranking []
  (let [ranking @(rf/subscribe [::subs/ranking])
        sortedusers (distinct (map :player ranking))
        findfreq (fn [player pos]
                   (as-> ranking x
                         (filter #(and (= (:player %) player) (= (:position %) pos)) x)
                         (first x)
                         (get x :freq 0)))
        medal (as-> ranking x
                    (group-by :position x)
                    (map (fn [t] (-> t val first)) x)
                    (into [] x))
        hastopmedal (fn [player pos]
                      (as-> (get medal pos) x
                            (:player x)
                            (= x player)))]

    [:section
     [:h1 "Olympic Ranking"]
     [:table.ranking
      [:tbody
       [:tr [:th "Player"] [:th.gold "Gold"] [:th.silver "Silver"] [:th.bronze "Bronze"]]
       (map-indexed (fn [idx p]
                      [:tr {:key (str idx p)} [:td (str (inc idx) "." p)]
                       [:td {:class-name (if (hastopmedal p 0) "gold" "")} (findfreq p 0)]
                       [:td {:class-name (if (hastopmedal p 1) "silver" "")} (findfreq p 1)]
                       [:td {:class-name (if (hastopmedal p 2) "bronze" "")} (findfreq p 2)]])
                    sortedusers)]]]))




;; a score row renderer (output a tr element)
(defn- record-row [r]
  (let [players @(rf/subscribe [::subs/get-players])
        trackname (:track r)
        times (:times r)
        gbx (get r :gbx "#")
        podium (mapv first (take 3 (sort-by val times)))]

    [:tr {:key trackname} [:td [:a {:href gbx} trackname]]
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
(defn score-table []
  ;; a simple table that displays the records stored in the database
  (let [players @(rf/subscribe [::subs/get-players])
        records @(rf/subscribe [::subs/get-records])]

   [:section.scoreContainer
    [:h2 "Track record board"]
    [:table#scoreTable.scoreTable
     [:tbody
      (into [:tr [:th "Tracks"]] (for [p players] [:th p]))
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
   ;;[header]
   [ranking]
   [score-table]])
   ;;[footer]])

;; about
(defn about-panel []
  [:div
   [:h1 "About"]
   [:p "Powered by re-frame (a React based Clojurescript framework) and Google Firebase"]
   [:p "Style and images credits to Aymeric Malchrowicz"]
   [:p "Source code is available on " [:a {:href "https://github.com/olivergg/tmrecords"} "github"]]
   [:div
    [:a {:href "#/"}
     "Back to Home Page"]]])



(defn- panels [panel-name]
  [:div
   [header]
   (case panel-name
     :home-panel [home-panel]
     :about-panel [about-panel]
     [:div])
   [footer]])

(defn show-panel [panel-name]
  [panels panel-name])

;; main
(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
