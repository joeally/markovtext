(ns markovtext.transitiontable
  (:gen-class))

(defn concat-bag [bag1 bag2]
  (->> (concat bag1 bag2)
       (group-by first)
       (map (fn [[k v]] [k (apply + (map second v))]))
       (into {})))

(defprotocol TransitionTable
  "A protocol for a transition table"
  (rand-next [tt prev])
  (rand-first [tt])
  (concat-tt [tt1 tt2])
  (add-transition-vec [tt from to-vec])
  (add-transition [tt from to]))

(extend-protocol TransitionTable
  clojure.lang.PersistentArrayMap
  (add-transition [tt from to]
    (update-in tt [from to] (fnil inc 0)))
  (add-transition-vec [tt from to-vec]
    (reduce #(add-transition %1 from %2) tt to-vec))
  (concat-tt [tt1 tt2]
    (into {} (for [[from to-bag] tt1] (concat-bag to-bag (get tt2 from {})))))
  (rand-first [tt]
    (rand-nth (keys tt)))
  (rand-next [tt prev]
    (rand-nth (apply concat (for [[k n] (get tt prev)] (repeat n k))))))



