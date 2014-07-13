(ns markovtext.transitiontable
  (:gen-class))

(defprotocol TransitionTable
  "A protocol for a transition table"
  (rand-next [tt prev])
  (rand-first [tt])
  (add-transition [tt from to]))

(extend-protocol TransitionTable
  clojure.lang.PersistentArrayMap

  (add-transition [tt from to]
    (update-in tt [from to] (fnil inc 0)))

  (rand-first [tt]
    (rand-nth (keys tt)))

  (rand-next [tt prev]
    (rand-nth (apply concat (for [[k n] (get tt prev)] (repeat n k))))))
