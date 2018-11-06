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
  (let [centisec (js/parseInt (goog.string/format "%.2f" (* seconds 100)))
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



(defn- render-delta-score [deltap]
  (as-> deltap x
        (vals x)
        (let [[{avg :avg  count :count}] x]
           (gstring/format "+%s(%s)" (readable-duration avg) count))))




;; olympic ranking table
(defn ranking []
  (let [ranking @(rf/subscribe [::subs/ranking])
        sortedusers (distinct (map :player ranking))
        findfreq (fn [player pos]
                   (as-> ranking x
                         (filter #(and (= (:player %) player) (= (:position %) pos)) x)
                         (first x)
                         (get x :freq 0)))
        totalfreq (fn [player]
                    (as-> ranking x
                          (filter #(and (= (:player %)  player) (<= (:position %) 2)) x)
                          (map :freq x)
                          (reduce + x)))
        medal (as-> ranking x
                    (group-by :position x)
                    (map (fn [t] (-> t val first)) x)
                    (into [] x))
        hastopmedal (fn [player pos]
                      (as-> (get medal pos) x
                            (:player x)
                            (= x player)))]

    [:section
     [:h1 "Olympic ranking"]
     [:table.ranking
      [:tbody
       [:tr [:th "Player"] [:th.gold "Gold"] [:th.silver "Silver"] [:th.bronze "Bronze"] [:th "Total"]]
       (map-indexed (fn [idx p]
                      [:tr {:key (str idx p)} [:td (str (inc idx) "." p)]
                       [:td {:class-name (if (hastopmedal p 0) "gold" "")} (findfreq p 0)]
                       [:td {:class-name (if (hastopmedal p 1) "silver" "")} (findfreq p 1)]
                       [:td {:class-name (if (hastopmedal p 2) "bronze" "")} (findfreq p 2)]
                       [:td (totalfreq p)]])
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


;;delta podium
(defn podiums []
  (let [p @(rf/subscribe [::subs/deltastobest])
        [firstp secondp thirdp & rest] p
        secondplayer (first (keys secondp))
        firstplayer (first (keys firstp))
        thirdplayer (first (keys thirdp))]

    [:section#podium.podium
     [:h1 "Deltas podium"]
     [:div.rank
      [:div.second.bloc
       [:div#tc2.name secondplayer [:br] (render-delta-score secondp)]
       [:div.step " "]
       " "]
      [:div.first.bloc
       [:div#tc1.name firstplayer [:br] (render-delta-score firstp)]
       [:div.step " "]
       " "]
      [:div.third.bloc
       [:div#tc3.name thirdplayer [:br] (render-delta-score thirdp)]
       [:div.step " "]
       " "]]
     [:div "You must complete at least 80% of all the tracks to be ranked"]]))





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

;; home, record board
(defn home-panel []
  [:div
   [ranking]
   [score-table]
   [podiums]])

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
