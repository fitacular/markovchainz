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

(defn key? [k]
  (seq (wcar* (car/keys k))))

(defn rset [k v]
  (wcar* (car/set k v)))

(defn rget [k]
  (wcar* (car/get k)))

(defn rand-str [n]
  (let [chars (map char (concat (range 48 58) (range 97 123)))]
    (clojure.string/join (take n (repeatedly #(rand-nth chars))))))

(defn get-random-key []
  (let [key (rand-str 6)]
   (if-not (key? key)
     key
     (recur))))
