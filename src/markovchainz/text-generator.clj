(ns markovchainz.text-generator)

; 1st order markov chain generator for preset toy model

(defn get-map
  []
  {
    "a" '(b),
    "b" '(c),
    "c" '(a b c),
  })

(defn chain
  [model curr-seq n state]
  (let
    [new-state (str (rand-nth (get model state)))]
    (if (pos? n)
      (conj (chain model curr-seq (dec n) new-state) new-state)
      curr-seq)))

(defn -main
  [& args]
  (let 
    [model (get-map)
     state (str (rand-nth (rand-nth (keys model))))
     len 20]
    (println (conj (chain model nil len state) state))))
