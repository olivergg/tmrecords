(ns tmrecords.subs
  (:require
   [re-frame.core :as re-frame]))


;; helper function
(defn mean [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count)
      {:avg (/ sum count) :count count}
      {:avg 0 :count 0})))

(defn times-sorter
  "A sorter for times TODO : add timestamp based tie breaker when sorting"
  [times]
  (sort-by val times))



;;; default subscriptions
(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))







;;;;;;;;;;;;;;;;;;;;;;;;
;; our subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;
(re-frame/reg-sub
  ::last-updated
  (fn [db]
    (:last-updated db)))

(re-frame/reg-sub
  ::get-mincount
  (fn [db]
    (:mincount db)))

(re-frame/reg-sub
 ::get-players
 (fn [db _]
   (as-> (:players db) x
     (map :name x))))

(re-frame/reg-sub
 ::get-records
 (fn [db _]
   (:records db)))

(re-frame/reg-sub
  ::ranked-records
  :<- [::get-records]
  (fn [records]
    (for [r records
          :let [timesorted (times-sorter (:times r))
                players-ranks (into {} (map-indexed (fn [idx v] {(first v) idx}) timesorted))]]
         (assoc r :timessorted timesorted
                  :ranking players-ranks
                  :best (second (first timesorted))))))

(re-frame/reg-sub
  ::deltas-podium
  :<- [::ranked-records]
  :<- [::get-mincount]
  (fn [[records mincount] _]
    (as-> (for [r records]
            {:track          (:track r)
             :diff-with-best (into {} (map (fn [[player time]] {player [(- time (:best r))]})
                                           (:timessorted r)))}) x
      (reduce (fn [m1 m2] {:diff-with-best (merge-with into (:diff-with-best m1) (:diff-with-best m2))}) x)
      (:diff-with-best x)
      (map (fn [[k v]] (merge {:player k} (mean v))) x)
      (filter (comp #(>= % (* mincount (count records))) :count)  x)
      (sort-by :avg x)
      (take 3 x))))


(re-frame/reg-sub
  ::count-records-by-player
  :<- [::ranked-records]
  (fn [records]
    (frequencies (filter string? (flatten (map #(:timessorted %) records))))))

(re-frame/reg-sub
  ;; Example output of this subscription
  ;({:position 0, :player "Mathieu", :freq 15}
  ; {:position 0, :player "Aymeric", :freq 8}
  ; {:position 0, :player "Olivier", :freq 5}
  ; {:position 1, :player "Olivier", :freq 10}
  ; {:position 1, :player "Alexandre", :freq 9}
  ; {:position 1, :player "Mathieu", :freq 5}
  ; {:position 1, :player "Aymeric", :freq 4}
  ; {:position 2, :player "Aymeric", :freq 13}
  ; {:position 2, :player "Olivier", :freq 6}
  ; {:position 2, :player "Mathieu", :freq 6}
  ; {:position 2, :player "Alexandre", :freq 1}
  ; {:position 3, :player "Olivier", :freq 7}
  ; {:position 3, :player "Alexandre", :freq 3}
  ; {:position 3, :player "Aymeric", :freq 3}
  ; {:position 3, :player "Sofiane", :freq 1})
  ::ranking
  :<- [::get-players]
  :<- [::ranked-records]
  :<- [::count-records-by-player]
  :<- [::get-mincount]
  (fn [[players records countbyplayer mincount] _]
    (def minimalcount-tiebreaker (comp (fn [t] (if (< t (* mincount (count records))) 2 1))
                                       countbyplayer
                                       :player))
    (as-> (for [r records]
               (for [p players
                      :let [position (get (:ranking r) p -1)]
                      :when (>= position 0)]
                  {:position position :player p})) x
          (flatten x)
          (frequencies x)
          (map (fn[[k f]] {:position (:position k) :player (:player k) :freq f}) x)
          (sort-by (juxt minimalcount-tiebreaker :position (comp - :freq)) x)
          x)))

(re-frame/reg-sub
  ::sorteduser
  :<- [::ranking]
  (fn [ranking]
    (distinct (map :player ranking))))

(re-frame/reg-sub
  ::get-player-rank-freq
  :<- [::ranking]
  (fn [ranking [_ player rank]]
    (as-> ranking x
      (filter #(and (= (:player %) player) (= (:position %) rank)) x)
      (first x)
      (get x :freq 0))))

(re-frame/reg-sub
  ::get-player-total-medal
  :<- [::ranking]
  (fn [ranking [_ player]]
    (as-> ranking x
      ;; medal are position 0 (gold) 1 (silver) and 2 (bronze)
      (filter #(and (= (:player %)  player) (<= (:position %) 2)) x)
      (map :freq x)
      (reduce + x))))

(re-frame/reg-sub
  ::is-player-first-for-medal
  :<- [::ranking]
  (fn [ranking [_ player rank]]
      (as-> ranking x
             (group-by :position x)
             (map (fn [t] (-> t val first)) x)
             (into [] x)
             (get x rank)
             (:player x)
             (= x player))))

(re-frame/reg-sub
  :user
  (fn [db _]
    (:user db)))

