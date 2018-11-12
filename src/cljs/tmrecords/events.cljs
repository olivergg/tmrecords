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
 (fn [a b] (js/alert (str "error" a b))))

;; update the players in the local db using the given value (coming from a firebase snapshot event)
;; example value [{:name "Jane"} {:name "Tarzan"}]
(re-frame/reg-event-db
  :players-updatedb
  (fn [db [_ players]]
    (assoc db :players players)))


;; update the records in the local db using the given value (coming from a firebase snapshot event)
(re-frame/reg-event-db
  :records-updatedb
  (fn [db [_ records]]
   ;; change all records so that the :times field is transformed :
   ;; before :times [{:player P1 :time t1 :tstamp 0},  {:player P2 :time t2}, ... ]
   ;; after :times {P1 {:time t1 :tstamp 0}, P2 {:time t2} , ...}
   (let [newrecords (map (fn [r] (update r
                                         :times
                                         (fn [oldtimes]
                                           (reduce (fn [m oldentry]
                                                     (assoc m (:player oldentry)
                                                              (if-let [old (:tstamp oldentry)]
                                                                {:time (:time oldentry) :tstamp old}
                                                                {:time (:time oldentry)})))
                                                   {}
                                                   oldtimes))))

                         records)]
     (assoc db :records newrecords))))


;; helper function to extract the data and id field from a firestore collection and convert it to a clojurescript map
(defn extract-firestore-data [raw]
  (as-> raw x
    (:docs x)
    (map (juxt :id :data) x)
    (map (fn[[k v]] (assoc v :id k)) x)
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



(re-frame/reg-event-fx
  :update-record-time
  (fn [{db :db} [_ trackid player time]]
    ;; TODO : improve by using unionArray of firestore ? (to update a single element in an array instead of the whole array)
    {:firestore/update
     ;;FIXME : change :mock to :records once everything will be properly tested
     {:path [:mock trackid]
      :data {:times (as-> db x
                          (get-in x [:records])
                          (filter #(= trackid (:id %)) x)
                          (first x)
                          (:times x)
                          (assoc-in x [player :time] time)
                          (map (fn[[k v]]
                                 (if (:tstamp v)
                                  {:player k :time (:time v) :tstamp (:tstamp v)}
                                  {:player k :time (:time v)})) x))}
      :on-success #(prn "Success:" %)
      :on-failure #(js/alert (str "Error:" %))}}))

(re-frame/reg-event-fx
  ;; TODO : add a button to add a track ?
  :add-track
  (fn [_ [_ trackid trackname]] {:firestore/set {:path [:records (keyword trackid)]
                                                 :data {:track trackname}}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
