(ns tmrecords.db)



(def default-db
  {:name "re-frame"
   :last-updated #inst "2018-10-26"
   ;;:players [{:name "Aymeric"} {:name "Mathieu"}]
   ;;:tracks [{:name "Tortillas_01" :gbx "todo"} {:name "Faster_12" :gbx "todo2"}]
   #_(:records [ {:track "Tortillas_01"}]
               :gbx "todo"
               :times [{:player "Aymeric" :time 12.13} {:player "Mathieu" :time 100.34}]
              {:track "Faster_12"
               :gbx "todo"
               :times [{:player "Aymeric" :time 100.12} {:player "Mathieu" :time 90.2}
                       {:player "Olivier" :time 300}]})})
;; TODO : store users, tracks and records in the database
