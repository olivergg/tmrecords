(ns tmrecords.events
  (:require
   [re-frame.core :as re-frame]
   [tmrecords.db :as db]
   [com.degel.re-frame-firebase :as firebase]
   ;;[com.rpl.specter :as s]
   ;;[com.rpl.specter :as s :refer-macros [select transform]] ;; add in the Specter macros that you need
  ;; [clojure.data.json :as json]
   [clojure.pprint :refer [pprint]]))

   

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))








;;;;;;;;;;; FIREBASE STUFF ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Simple sign-in event. Just trampoline down to the re-frame-firebase
;;; fx handler.
(re-frame/reg-event-fx
 :sign-in
 (fn [_ _] {:firebase/google-sign-in {:sign-in-method :popup}}))


;;; Ditto for sign-out
(re-frame/reg-event-fx
 :sign-out
 (fn [_ _] {:firebase/sign-out nil}))


;;; Store the user object
(re-frame/reg-event-db
 :set-user
 (fn [db [_ user]]
   (assoc db :user user)))

;; custom handler on firebase error to print the error message in the console
(re-frame/reg-event-fx
 :firebase-error
 (fn [a b] (prn "error" a b)))

;; update the players in the local db using the given value
;; example value [{:name "Jane"} {:name "Tarzan"}]
(re-frame/reg-event-db
  :players-updatedb
  (fn [db [_ value]]
    (assoc db :players value)))


;; update the records in the local db using the given value
(re-frame/reg-event-db
 :records-updatedb
 (fn [db [_ value]]
   ;; for all records,
   ;; change the shape of the :times field
   ;; from [{:player P1 :time t1} {:player P2 :time t2}] to {P1 t1, P2 t2}
   (assoc db :records
          (map #(assoc % :times (reduce-kv (fn [m k v] (assoc m (:player v) (:time v))) {} (:times %)))
           value))))

;; helper function to extract the data field from a firestore collection and convert it to a clojurescript map
(defn extract-firestore-data [raw]
  (as-> raw x
    (:docs x)
    (map :data x)
    (map clojure.walk/keywordize-keys x)))

(re-frame/reg-event-fx
  :players-listen
  (fn [_ _] {:firestore/on-snapshot {:path-collection [:players]
                                     :doc-changes false
                                     :on-next #(do (.log js/console "players collection has changed")
                                                   (re-frame/dispatch [:players-updatedb (extract-firestore-data %)]))}}))
(re-frame/reg-event-fx
 :records-listen
 (fn [_ _] {:firestore/on-snapshot {:path-collection [:records]
                                    :doc-changes false
                                    :on-next #(do (.log js/console "records collection has changed")
                                                  (re-frame/dispatch [:records-updatedb  (extract-firestore-data %)]))}}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
