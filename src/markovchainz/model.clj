(ns markovchainz.model
  [:require
    [markovchainz.redis :as redis]
    [markovchainz.songs :as songs]])

(defn add [x]
  (do
    (redis/add-to-set (butlast x) (last x))))

(defn setup []
  (let [k 2]
    (doall (map #(map add (partition (inc k) 1 %)) (songs/song-stream)))))
