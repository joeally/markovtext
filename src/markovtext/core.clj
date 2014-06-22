(ns markovtext.core
  (:gen-class)
  (:require
   [clojure.set])
  (:use
   incanter.core))

(defn m-chain [transition init]
  "returns a function which returns the probabilities for each state after n steps when given a transition matrix and initial probabilities"
  (let [mtransition (matrix transition) minit (matrix init)]
    (fn [n]
      (reduce mmult (cons (trans minit) (repeat n mtransition)))))) ;; mmult over the initial probabilities and n transition matrices

(defn quite-likely-seq
  ([prev col-trans n cust-get]
     (let [choices (vec (cust-get col-trans prev))]
       (let [choice (get choices (rand-int (count choices)))]
         (lazy-seq (cons
                    (first prev)
                    (quite-likely-seq (conj (vec (drop 1 prev)) choice) col-trans n cust-get))))))
  ([prev col-trans n] (quite-likely-seq prev col-trans n get)))

(defn collect-transitions
  ([seqn n]
     (let [transitions (partition (inc n) 1 seqn)]
       (into
        {}
        (for [[k v] (group-by #(take n %) transitions)] [k (map last v)]))))
  ([seqn] (collect-transitions seqn 1)))

(defn concat-transitions [t1 t2]
  (into {}
        (for [k (clojure.set/union (keys t1) (keys t2))]
          [k (concat (get t1 k '()) (get t2 k '()))])))

(defn transition-probs [col-trans poss-syms]
  "Takes a list of possible symbols and a map containing a symbol to a list of symbols that have appeared after it and creates a map detailing the transition probabilities"
  (into {} (for [[k v] col-trans]
             [k (into {} (for [sym poss-syms]
                           [sym (/ (count (filter #(= sym %) v)) (count v))]))])))

(defn init-probs [col-trans]
  (let [num-trans (sum (map (comp count second) col-trans))]
    (into {}  (for [[k v] col-trans]
                [k (/ (count v) num-trans)]))))


(defn process-corpus [corpus]
  (for [doc corpus]
    (-> doc
         (clojure.string/lower-case)
         (clojure.string/replace #"[\.,?]" #(clojure.string/join [" " (str %)]))
         (clojure.string/replace #"(\\n){1,}" ""))))

(defn corpus-transitions
  ([corpus] (corpus-transitions corpus 1))
  ([corpus n]
     (->> corpus
          (map #(clojure.string/split % #" "))
          (map #(collect-transitions % n))
          (reduce concat-transitions))))

(defn corpus-starts [corpus n]
  (map (partial take n) corpus))

(defn corpus-likely-seq [corpus n]
  (let [transitions (corpus-transitions (process-corpus corpus) n)]
    (let [start (get (vec (keys transitions)) (rand-int (count (keys transitions))))]
      (quite-likely-seq start transitions n))))

(defn stringify-seq [seq]
  (->>
   seq
   (replace { "i" "I"})
   (clojure.string/join " ")
   (#(clojure.string/replace % #" +[\.\?,]" (fn [s] (str (second s)))))))


(defn -main
  "I don't do a whole lot ... yet."
  []
  (println "some message"))
