(ns markovchainz.redis
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn clear-redis []
  (map #(wcar* (car/del %)) (wcar* (car/keys "*"))))

(defn add-to-set
  [set-name item]
  (wcar* (car/sadd set-name item)))

(defn get-set
  [set-name]
  (wcar* (car/smembers set-name)))
