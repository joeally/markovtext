(ns markovtext.transitiontable
  (:gen-class))

(defn concat-bag [bag1 bag2]
  (merge-with + bag1 bag2))

(defprotocol TransitionTable
  "A protocol for a transition table"
  (rand-next [tt prev])
  (rand-first [tt])
  (concat-tt [tt1 tt2])
  (add-transition-vec [tt from to-vec])
  (add-transition [tt from to]))

(extend-protocol TransitionTable
  clojure.lang.IPersistentMap

  (add-transition [tt from to]
    (update-in tt [from to] (fnil inc 0)))

  (add-transition-vec [tt from to-vec]
    (reduce #(add-transition %1 from %2) tt to-vec))

  (concat-tt [tt1 tt2]
    (merge-with concat-bag tt1 tt2))

  (rand-first [tt]
    (rand-nth (keys tt)))

  (rand-next [tt prev]
    (try
      (rand-nth (apply concat (for [[k n] (get tt prev)] (repeat n k))))
      (catch IndexOutOfBoundsException e nil))))
