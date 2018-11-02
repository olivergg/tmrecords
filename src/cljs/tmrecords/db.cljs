(ns tmrecords.db)



(def default-db
  {:name "re-frame"
   :last-updated #inst "2018-10-26"
   :records {} ;; will be filled with a listen event to the firestore database
   :players {}}) ;; will be filled with a listen event to the firestore database})



