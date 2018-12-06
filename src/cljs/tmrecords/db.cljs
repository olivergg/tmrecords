(ns tmrecords.db)



(def default-db
  {:name               "tmrecords"
   :last-updated       #inst "2018-10-26" ;;TODO implement this
   :track-filter-value "" ;; the track filtering string, default to empty string
   :mincount           0.8 ;; minimal ratio of tracks to complete to be ranked
   :records            {} ;; will be filled with a listen event to the firestore database
   :players            {}}) ;; will be filled with a listen event to the firestore database})



