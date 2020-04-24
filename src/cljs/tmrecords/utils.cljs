(ns tmrecords.utils
  (:require [re-frame.core :as re-frame]))

(defn negligible?
  "Variant of empty? that behaves reasonably for non-seqs too.
  Note that nil is negligible but false is not negligible."
  [x]
  (cond (seqable? x) (empty? x)
        (boolean? x) false
        :else (not x)))

(defn >evt
  "Shorthand for re-frame dispatch to event.
  The two-argument form appends a value into the event.
  The three-argument form offers more control over this value, letting
  you specify a default value for it and/or a coercer (casting) function"
  ([event]
   (re-frame/dispatch event))
  ([event value]
   (re-frame/dispatch (conj event value)))
  ([event value {:keys [default coercer] :or {coercer identity}}]
   (>evt event
         (coercer (if (negligible? value)
                    default
                    value)))))

(defn <sub
  "Shorthand for re-frame subscribe and deref."
  ([subscription]
   (-> subscription re-frame/subscribe deref))
  ([subscription default]
   (or (<sub subscription) default)))