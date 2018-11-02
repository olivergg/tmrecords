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

(re-frame/reg-sub
  ::ranking
  :<- [::get-players]
  :<- [::get-records]
  (fn [[players records] _]

    (as-> (for [r records]
           (let [trackname (:track r)
                 times (:times r)
                 gbx (get r :gbx "#")
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

