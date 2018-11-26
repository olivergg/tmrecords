(ns tmrecords.upload
  (:require [reagent.core :refer [render atom]]
            [cljs.core.async :refer [put! chan <! >!]]
            [tmrecords.subs :as subs]
            [iron.re-utils :refer [<sub >evt]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; copy pasted from https://gist.github.com/paultopia/6fc396884c223b619f2e2ef199866fdd

;; derived from https://mrmcc3.github.io/post/csv-with-clojurescript/
;; and based on reagent-frontend template.

;; dependencies from project.clj in addition to clojure, clojurescript, and reagent:
;; [org.clojure/core.async "0.2.395"]

;; atom to store file contents

(def file-data (atom {}))


(defn extract-besttime
  "Extract the best time from the string of the gbx file"
  [rawstr]
  (let [header (subs rawstr 0xA9 0X163)]
    (as-> header x
          (re-find #"<times best=\"(\d+)\"" x)
          (second x)
          (js/Number x)
          (/ x 1000))))


;; transducer to stick on a core-async channel to manipulate all the weird javascript
;; event objects --- basically just takes the array of file objects or something
;; that the incomprehensible browser API creates and grabs the first one, then resets things.
(def first-file
  (map (fn [e]
         (let [target (.-currentTarget e)
               file (-> target .-files (aget 0))]
           ;;TODO FIXME : find a way to do this outside the transducer
           (let [[_ playeralias trackfilename] (re-matches #"(.*?)_(.*).Replay.gbx" (.-name file))
                 {player :name} (<sub [::subs/find-player-by-alias playeralias])
                 trackid (<sub [::subs/find-track trackfilename])]
             (if (and playeralias player trackid) ;; if match is successfull
               (swap! file-data assoc :player player :trackid trackid)
               (swap! file-data assoc :error "Invalid upload")))
           (set! (.-value target) "")
           file))))


;; transducer to get text out of file object.
(def extract-result
  (map #(as-> % x
              (.-target x)
              (.-result x)
              (js->clj x)
              (extract-besttime x))))

;; two core.async channels to take file array and then file and apply above transducers to them.
(def upload-reqs (chan 1 first-file))
(def file-reads (chan 1 extract-result))

;; function to call when a file event appears: stick it on the upload-reqs channel (which will use the transducer to grab the first file)
(defn put-upload [e]
  (put! upload-reqs e))

;; sit around in a loop waiting for a file to appear in the upload-reqs channel, read any such file, and when the read is successful, stick the file on the file-reads channel.
(go-loop []
         (let [reader (js/FileReader.)
               file (<! upload-reqs)]
           (set! (.-onload reader) #(put! file-reads %))
           (.readAsText reader file)
           (recur)))

;; sit around in a loop waiting for a string to appear in the file-reads channel and put it in the state atom to be read by reagent and rendered on the page.
(go-loop []
         (swap! file-data assoc :time (<! file-reads))
         (recur))

;; input component to allow users to upload file.
(defn input-component []
  [:input {:type "file" :id "file" :accept ".gbx" :name "file" :on-change put-upload}])

(defn upload-component []
  (let [fd @file-data
        {:keys [trackid player time]} @file-data
        hasdata (not (empty? fd))]
    [:section
     [:h2 "Upload a GBX replay"]
     [input-component]
     (when hasdata
       [:ul
        [:li "Track : " trackid]
        [:li "Player : " player]
        [:li "Time : " time]])
     [:span (cond
              (not hasdata) ""
              (not (:error fd))
              [:button
               {:on-click #(do (reset! file-data {})
                               (>evt [:update-record-time trackid player time]))} "Submit"]
              :else (:error fd))]]))
