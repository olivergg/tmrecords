(ns tmrecords.db)



(def default-db
  {:name "re-frame"
   :last-updated #inst "2018-10-26"
   :players [{:name "Aymeric"} {:name "Mathieu"}]
   :tracks [{:name "Tortillas_01" :gbx "todo"} {:name "Faster_12" :gbx "todo2"}]
   :records [ {:track "Tortillas_01"
               :times {"Aymeric" 12.13 "Mathieu" 100.34}}
              {:track "Faster_12"
               :times {"Aymeric" 100.1 "Mathieu" 90.2 "Olivier" 300}}]})
;; TODO : store users, tracks and records in the database
