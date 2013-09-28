(ns markovchainz.songs
  [:require
   [markovchainz [redis :as redis]]
   [clojure.string :as str]])

(defn get-stream [lines]
  (map #(conj (into [:start :start] (str/split % #" ")) :end)
       (map
        #(str/lower-case (str/replace % #"[(\")]" "")) lines)))

(defn song-stream []
  (get-stream (flatten (map :lyrics (redis/get-set "songs")))))
