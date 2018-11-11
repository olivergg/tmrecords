(ns tmrecords.views
  (:require
   [re-frame.core :as rf]
   [tmrecords.subs :as subs]
   [iron.re-utils :refer [<sub >evt]]
   [goog.string :as gstring]
   goog.string.format))

;; constants
;; some predefined colors
(defonce colors ["gold", "silver", "#cd7f32","#FF34FF", "#008941", "#006FA6", "#A30059"])

;; helper functions
(defn readable-duration
  "Format a number of seconds to a readable format (mm:ss.SSS) "
  [seconds]
  (let [;; format then reparse to prevent precision issue (for example 9.54 * 100)
        centisec (js/parseInt (goog.string/format "%.2f" (* seconds 100)))
        cent-r (rem centisec 100)
        minutes-int (quot seconds 60)
        minutes-r (rem seconds 60)]
    (if (>= seconds 0)
      (gstring/format "%02d:%02d.%02d" minutes-int minutes-r cent-r)
      "-")))

(defn render-podium-label
  "Render a delta score on the podium (average delta + count)"
  [{:keys [avg count]}]
  (gstring/format "+%s(%s)" (readable-duration avg) count))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn header
  "The green header :-) "
  []
  [:div.header-wrapper [:section.header
                        [:img
                         {:width "150",
                          :src "tm-main.png",
                          :alt "tm-main.png"}]
                        [:div.bigTitle "TrackMania Records"]]])


(defn footnotelink
  "A span to link to the bottom of the page"
  []
  [:span [:a {:href "#footnote"} " *"]])


(defn ranking
  "Render the Olympic Ranking table"
  []
  (let [isplayerfirstformedal (<sub [::subs/is-player-first-for-medal-fn])
        getplayertotalmedal (<sub [::subs/get-player-total-medal-fn])
        getplayerrankfreq (<sub [::subs/get-player-rank-freq-fn])
        sortedusers (<sub [::subs/sorteduser])]
    [:section
     [:h1 "Olympic ranking" (footnotelink)]
     [:table.ranking
      [:tbody
       [:tr [:th "Player"] [:th.gold "Gold"] [:th.silver "Silver"] [:th.bronze "Bronze"] [:th "Total"]]
       (doall (map-indexed (fn [idx p]
                             [:tr {:key (str idx p)} [:td (str (inc idx) "." p)]
                              [:td {:class-name (if (isplayerfirstformedal p 0) "gold" "")}
                               (getplayerrankfreq p 0)]
                              [:td {:class-name (if (isplayerfirstformedal p 1) "silver" "")}
                               (getplayerrankfreq p 1)]
                              [:td {:class-name (if (isplayerfirstformedal p 2) "bronze" "")}
                               (getplayerrankfreq p 2)]
                              [:td (getplayertotalmedal p)]])
                           sortedusers))]]]))


(defn visualspread
  "Render a visual div spread of the given series of numbers"
  [series]
  (let [deltatofirst-series (map #(- % (first series)) series)
        maxdiff (last deltatofirst-series)
        leftseries (map (fn [v] (/ (* v 90.0) (float maxdiff))) deltatofirst-series)]
    [:div#visualdiff-container
     [:div#visualdiff-inner
      (map-indexed (fn [idx x] [:div.visualdiff-child {:key   idx
                                                       :style {:background-color (colors (mod idx 7))
                                                               :left             (str x "px")}}])
                   leftseries)]]))

(defn score-row
  "Render a row in the score table (output a tr element)"
  [{:keys [times ranking track gbx timessorted]}]
  [:tr [:td [:a {:href (or gbx "#")} track]]
   (for [p (<sub [::subs/get-players])
         :let [position (get ranking p -1)
               duration (readable-duration (get times p "-"))]]
     ^{:key p}
     [:td {:class-name (case position
                         0 "best"
                         1 "secondbest"
                         2 "thirdbest"
                         "")}
      duration])
   [:td {:style {:width "100px"}}
    (visualspread (map second timessorted))]])


(defn score-table
  "A simple table that displays the records stored in the database for each tracks"
  []
  [:section.scoreContainer
   [:h2 "Track record board"]
   [:table#scoreTable.scoreTable
    [:tbody
     [:tr [:th "Track"] (for [p (<sub [::subs/get-players])] ^{:key p} [:th p]) [:th "Spread"]]
     (for [r (<sub [::subs/ranked-records])]
       ^{:key (:track r)} [score-row r])]]])


(defn deltas-podiums
  "Render the delta podium (top 3 average time gap with the best time)"
  []
  (let [p (<sub [::subs/deltas-podium])
        [firstp secondp thirdp & _] p
        {secondplayer :player} secondp
        {firstplayer :player} firstp
        {thirdplayer :player} thirdp]
    [:section#podium.podium
     [:h1 "Deltas podium" (footnotelink)]
     [:div.rank
      [:div.second.bloc
       [:div#tc2.name secondplayer [:br] (render-podium-label secondp)]
       [:div.step " "]
       " "]
      [:div.first.bloc
       [:div#tc1.name firstplayer [:br] (render-podium-label firstp)]
       [:div.step " "]
       " "]
      [:div.third.bloc
       [:div#tc3.name thirdplayer [:br] (render-podium-label thirdp)]
       [:div.step " "]
       " "]]]))

(defn footer
  "The footer"
  []
  (let [lastupd (<sub [::subs/last-updated])
        user (<sub [:user])
        connected (not (nil? (:display-name user)))]
   [:section.footer
    [:a (get user :display-name "Not connected")]
    (if connected
      [:a {:href "#" :on-click #(>evt [:sign-out])} "Sign out"]
      [:a {:href "#" :on-click #(>evt [:sign-in])} "Sign in"])
    [:a {:href "#/about"} "About"]
    [:a#lastupdatedlnk (str "Last updated : " lastupd)]]))


(defn home-panel
  "Render the home page"
  []
  [:div
   [ranking]
   [score-table]
   [deltas-podiums]
   [:div#footnote (gstring/format "* You must complete at least %d% of all the tracks to be ranked"
                                  (* 100 (<sub [::subs/get-mincount])))]])

(defn about-panel
  "Render the about page"
  []
  [:div
   [:h1 "About"]
   [:p "Powered by re-frame (a React based Clojurescript framework) and Google Firebase for the storage"]
   [:p "Style and images credits to Aymeric Malchrowicz"]
   [:p "Source code is available on " [:a {:href "https://github.com/olivergg/tmrecords"} "github"]]
   [:p "Largely inspired by " [:a {:href "https://github.com/jakemcc/backgammon"} "the Backgammon game of Jake McCrary"]]
   [:div
    [:a {:href "#/"}
     "Back to Home Page"]]])

(defn- panels
  "Render the active panel"
  [panel-name]
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
(defn main-panel
  "Main entry point"
  []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
