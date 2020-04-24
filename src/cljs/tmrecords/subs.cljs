(ns tmrecords.subs
  (:require
   [re-frame.core :as re-frame]))


;; helper functions
(defn mean
  "Compute the average and count value of a collection of number.
  Returns a map {:avg XXXX :count NN }"
  [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count)
      {:avg (/ sum count) :count count}
      {:avg 0 :count 0})))

(defn times-sorter
  "A sorter for times, use tstamp as tie breaker"
  [times]
  (as-> (sort-by (comp (juxt :time :tstamp) val) times) x
        (map (fn [t] [(key t) ((comp :time val) t)]) x)))

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
  ::find-player-by-alias
  (fn [db [_ alias]]
    (as-> (:players db) x
          (filter #(some #{alias} (:aliases %)) x)
          (first x))))

(re-frame/reg-sub
  ::find-track
  (fn [db [_ trackname]]
    (as-> (:records db) x
          (filter #(= (-> % :track .toLowerCase) (.toLowerCase trackname)) x)
          (first x)
          (:id x))))




(re-frame/reg-sub
 ::get-records
 (fn [db x]
   (:records db)))

(re-frame/reg-sub
  ::get-track-filter-value
  (fn [db _]
    (:track-filter-value db)))


(re-frame/reg-sub
  ::ranked-records
  :<- [::get-records]
  :<- [::get-track-filter-value]
  (fn [[records filterstr] _]
    (for [r records
          :when (clojure.string/includes? (-> r :track .toLowerCase) (-> filterstr .toLowerCase))
          :let [timesorted (times-sorter (:times r))
                players-ranks (into {} (map-indexed (fn [idx v] {(first v) idx}) timesorted))]]
         (assoc r :timessorted timesorted
                  :times (into {} timesorted)
                  :ranking players-ranks
                  :isvalid (>=  (count timesorted) 3)
                  :best (second (first timesorted))))))


(re-frame/reg-sub
 ::count-valid-records
 :<- [::ranked-records]
 (fn [records]
   (count (filter :isvalid records))))

(re-frame/reg-sub
  ::deltas-podium
  :<- [::ranked-records]
  :<- [::get-mincount]
  :<- [::count-valid-records]
  (fn [[records mincount countvalidrecords] _]
    (as-> (for [r records
                :when (:isvalid r)]
            {:track          (:track r)
             :diff-with-best (into {} (map (fn [[p time]] {p [(- time (:best r))]})
                                           (:timessorted r)))}) x
      (reduce (fn [m1 m2] {:diff-with-best (merge-with into (:diff-with-best m1) (:diff-with-best m2))}) x)
      (:diff-with-best x)
      (map (fn [[p player-times]] (merge {:player p} (mean player-times))) x)
      (filter (comp #(>= % (* mincount countvalidrecords)) :count)  x)
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
  :<- [::count-valid-records]
  (fn [[players records countbyplayer mincount countvalidrecords] _]
    (def minimalcount-tiebreaker (comp (fn [t] (if (< t (* mincount countvalidrecords)) 2 1))
                                       countbyplayer
                                       :player))
    (as-> (for [r records
                :when (:isvalid r)]
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
  ::olympic-ranking
  :<- [::ranking]
  :<- [::sorteduser]
  (fn [[ranking sortedusers] _]
    (letfn [(get-player-rank-freq [player rank]
              (as-> ranking x
                    (filter #(and (= (:player %) player) (= (:position %) rank)) x)
                    (first x)
                    (get x :freq 0)))
            (get-player-total-medal [player]
              (as-> ranking x
                    ;; medal are position 0 (gold) 1 (silver) and 2 (bronze)
                    (filter #(and (= (:player %)  player) (<= (:position %) 2)) x)
                    (map :freq x)
                    (reduce + x)))
            (is-player-first-for-medal [player rank]
              (as-> ranking x
                    (group-by :position x)
                    (map (fn [t] (-> t val first)) x)
                    (into [] x)
                    (get x rank)
                    (:player x)
                    (= x player)))]
      (as-> sortedusers x
            (map-indexed (fn [idx p]
                           {::idx idx
                            ::player p
                            ::isfirstforgold (is-player-first-for-medal p 0)
                            ::numberofgold (get-player-rank-freq p 0)
                            ::isfirstforsilver (is-player-first-for-medal p 1)
                            ::numberofsilver (get-player-rank-freq p 1)
                            ::isfirstforbronze (is-player-first-for-medal p 2)
                            ::numberofbronze (get-player-rank-freq p 2)
                            ::totalnumber (get-player-total-medal p)}) x)
            (into [] x)))))


(re-frame/reg-sub
  :user
  (fn [db _]
    (:user db)))

