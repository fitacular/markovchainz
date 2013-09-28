(ns markovchainz.model
  [:require
    [clojure.string :as str]
    [markovchainz.redis :as redis]
    [markovchainz.songs :as songs]
    [taoensso.carmine :as car :refer (wcar)]])

(defn add [x]
  (redis/add-to-set (reverse (pop (reverse x))) (last x)))

(defn setup []
  (let [k 2]
    (doall (map add (partition (+ k 1) 1 (songs/song-stream))))))
