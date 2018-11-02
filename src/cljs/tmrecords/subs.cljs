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
  :user
  (fn [db _]
    (:user db)))

