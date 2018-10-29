(ns tmrecords.db)



(def default-db
  {:name "re-frame"
   :last-updated #inst "2018-10-26"
;;   :users [{:name "Aymeric"} {::name "Mathieu"}]
;;   :tracks [{:id 1 :name "Shorter 01" ::link "#"} {:id 2 :name "Tortillas 02" :link "#"}]
   :records [
              {:track "Tortillas_01"
               :times {"Aymeric" 200 "Mathieu" 100}}
              {:track "Faster_12"
               :times {"Aymeric" 100 "Mathieu" 90 "Olivier" 300}}]})
   
;; TODO : store users, tracks and records in the database
