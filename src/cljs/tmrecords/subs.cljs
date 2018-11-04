(ns tmrecords.subs
  (:require
   [re-frame.core :as re-frame]))

;;; default subscriptions
(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))



;; our subscriptions
(re-frame/reg-sub
  ::last-updated
  (fn [db]
    (:last-updated db)))

(re-frame/reg-sub
 ::get-players
 (fn [db _]
   (as-> (:players db) x
     (map :name x))))

(re-frame/reg-sub
 ::get-records
 (fn [db _]
   (:records db)))

(defn mean [coll]
  (let [sum (apply + coll)
        count (count coll)]
    (if (pos? count)
      {:avg (/ sum count) :count count}
      {:avg 0 :count 0})))

(re-frame/reg-sub
  ::deltastobest
  :<- [::get-records]
  (fn [records]
    (as-> (for [r records]
            (let [trackname (:track r)
                  times (:times r)
                  timesasc (sort-by val times)
                  besttime (second (first timesasc))]

              {:track trackname
               :deltatobest (into {} (map (fn [[k v]] {k [(- v besttime)]}) timesasc))})) x
      ;;(take 2 x)
      (reduce (fn [m1 m2] {:deltatobest (merge-with into (:deltatobest m1) (:deltatobest m2))}) x)
      (:deltatobest x)
      (map (fn [[k v]] {k (mean v)} ) x)
      (filter (comp #(>= % (* 0.8 (count records))) :count first vals)  x)
      (sort-by (comp :avg first vals) x))))



(re-frame/reg-sub
  ::ranking
  :<- [::get-players]
  :<- [::get-records]
  (fn [[players records] _]

    (as-> (for [r records]
           (let [times (:times r)
                 timesasc (mapv first (sort-by val times))]

                (for [p players
                      :let [position (.indexOf timesasc p)]
                      :when (>= position 0)]
                  {:position position :player p}))) x
          (flatten x)
          (frequencies x)
          (map (fn[[k f]] {:position (:position k) :player (:player k) :freq f}) x)
          (sort-by (juxt :position (comp - :freq)) x)
          ;; Example output
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
          x)))








(re-frame/reg-sub
  :user
  (fn [db _]
    (:user db)))

