(ns markovchainz.text-generator
  [:require
   [markovchainz.redis :as redis]
   [clojure.string :as str]])

(defn next-word [prefix]
  (rand-nth (redis/get-set prefix)))

(defn line
  ([]
     (line `[:markov-start :markov-start]))
  ([line-so-far]
     (let [k 2]
       (if (= "markov-end" (last line-so-far))
         (str/join " " (rest (rest (butlast line-so-far))))
         (recur (conj line-so-far
                      (next-word (take-last k line-so-far))))))))

(defn stanza []
  (let [lines 5]
    (str (str/join "\n" (repeatedly lines #(line))) "\n")))

(defn body []
  (let [stanzas 3]
    (str (str/join "\n" (repeatedly stanzas #(stanza))) "\n")))
