(ns tmrecords.firebase
 (:require [re-frame.core :as rf]
           [clojure.string :as string]
           [cljs.reader :as reader]
           [com.degel.re-frame-firebase :as firebase]))


(defonce firebase-app-info
       {:apiKey "AIzaSyBYhuNmwQ92TGZ4ecRexAShVZ55ozAP9po"
        :authDomain "tmrecords-fa4b2.firebaseapp.com"
        :databaseURL "https://tmrecords-fa4b2.firebaseio.com"
        :projectId "tmrecords-fa4b2"
        :storageBucket "tmrecords-fa4b2.appspot.com"
        :messagingSenderId "199244989408"})


(defn ^:export init[]
 (firebase/init :firebase-app-info      firebase-app-info
                :firestore-settings     {:timestampsInSnapshots true}
                :get-user-sub           [:user]
                :set-user-event         [:set-user]
                :default-error-handler  [:firebase-error])
 (rf/dispatch-sync [:players-listen])
 (rf/dispatch-sync [:records-listen]))




