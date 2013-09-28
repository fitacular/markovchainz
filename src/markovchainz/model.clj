(ns markovchainz.model
  [:require
    [clojure.string :as str]
    [markovchainz.redis :as redis]
    [taoensso.carmine :as car :refer (wcar)]])

; call lyrics fetchers
; build k-th order markov model
; add to redis

(defn get-str [] "All gold in my amenities")

(defn add [x]
  (redis/add-to-set (reverse (pop (reverse x))) (last x)))

(defn -main []
  (let [k 2]
    (doall (map add (partition (+ k 1) 1 (str/split (get-str) #"\s+"))))))
