(ns markovtext.core
  (:gen-class)
  (:require
   [clojure.set]
   [markovtext.redis :as redis]
   [markovtext.reddit :as reddit]
   [markovtext.transitiontable :as tt]
   [markovtext.cli :as cli])
  (:use
   incanter.core))

(defn quite-likely-seq [prev trans-tbl n]
  (map
   first
   (iterate
    (fn [prev]
      (conj (vec (drop 1 prev)) (tt/rand-next trans-tbl prev)))
    prev)))

(defn collect-transitions
  ([seqn n]
     (->> seqn
          ;;split sequence into overlapping sequences of n+1
          (partition (inc n) 1)
          ;;convert these sequnces into vector in the form [fromvec to]
          (map (fn [part] [(vec (take n part)) (last part)]))
          ;;collect transition vectors into a map
          (reduce (fn [tt [from to]] (tt/add-transition tt from to)) {})))
  ([seqn] (collect-transitions seqn 1)))

(defn process-corpus [corpus]
  (for [doc corpus]
    (-> doc
         (clojure.string/lower-case)
         (clojure.string/replace #"[\.,?:;]" #(clojure.string/join [" " (str %)]))
         (clojure.string/replace #"(\\n){1,}" "")
         (clojure.string/split #" "))))

(defn corpus-transitions
  ([corpus] (corpus-transitions corpus 1))
  ([corpus n]
     (->> corpus
          (map #(collect-transitions % n))
          (reduce tt/concat-tt))))

(defn corpus-likely-seq [corpus n]
  (let [transitions (corpus-transitions (process-corpus corpus) n)]
    (let [start (rand-nth (vec (keys transitions)))]
      (quite-likely-seq start transitions n))))

(defn stringify-seq [seq]
  (->>
   seq
   (replace { "i" "I"})
   (clojure.string/join " ")
   (#(clojure.string/replace % #" +[\.\?,]" (fn [s] (str (second s)))))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{opts :opts args :args} (apply cli/process-args args)] 
    (if (contains? opts "generate")
      (println
       (stringify-seq
        (take
         100;;(comp not nil?)
         (quite-likely-seq (redis/get-random-start 1) nil 1 #(redis/redis-get %2))))))))

