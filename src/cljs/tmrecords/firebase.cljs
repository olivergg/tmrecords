(ns tmrecords.firebase
 (:require [re-frame.core :as rf]
           [com.degel.re-frame-firebase :as firebase]))

(defonce firebase-app-info
       {:apiKey "AIzaSyBYhuNmwQ92TGZ4ecRexAShVZ55ozAP9po"
        :authDomain "tmrecords-fa4b2.firebaseapp.com"
        :databaseURL "https://tmrecords-fa4b2.firebaseio.com"
        :projectId "tmrecords-fa4b2"
        :storageBucket "tmrecords-fa4b2.appspot.com"
        :messagingSenderId "199244989408"})

;; atom to store whether firebase has been initiliazed
;; prevent duplicate-app message during development which cause Firebase to be initiliazed twice
(defonce firebase-initialized (atom false))

(defn ^:export init []
 (when-not @firebase-initialized

   (firebase/init :firebase-app-info      firebase-app-info
                  :get-user-sub           [:user]
                  :set-user-event         [:set-user]
                  :default-error-handler  [:firebase-error])
   (reset! firebase-initialized true)
   (prn "Firebase initialized")
   (rf/dispatch [:players-listen])
   (rf/dispatch [:records-listen])))