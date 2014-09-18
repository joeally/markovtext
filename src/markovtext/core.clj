(ns markovtext.core
  (:gen-class)
  (:require
   [clojure.set]
   [markovtext.redis :as redis]
   [markovtext.reddit :as reddit]
   [markovtext.TransitionTable :as tt]
   [com.gfredericks.debug-repl :refer [break! unbreak!]]
   [clojure.core.async :as async]
   [markovtext.cli :as cli])
  (:use
   incanter.core)
  (:import markovtext.redis.RedisConn))

(defn quite-likely-seq [fst trans-tbl]
  (map
   first
   (iterate
    (fn [prev]
      (conj (vec (drop 1 prev)) (tt/rand-next trans-tbl prev)))
    fst)))

(defn col-transitions [seqn n]
  (->> seqn
       (partition (inc n) 1)
       ;;convert transition to [from-vec to-symbol] pairs
       (map (fn [part] [(vec (drop-last part)) (last part)]))))

(defn make-transition-table [tbl transitions]
  (reduce (fn [tt [from to]] (tt/add-transition tt from to)) tbl transitions))

(defn process-corpus [corpus]
  (for [doc corpus]
    (-> doc
         (clojure.string/lower-case)
         (clojure.string/replace #"[\.,?:;]" #(clojure.string/join [" " (str %)]))
         (clojure.string/replace #"(\\n){1,}" "")
         (clojure.string/split #" ")
         ((fn [x] (remove empty? x))))))


(defn corpus-transitions
  ([tbl corpus n]
      (->> corpus
           (map #(col-transitions % n))
           (apply concat)
           (make-transition-table tbl)))
  ([tbl corpus] (corpus-transitions tbl corpus 1)))

(defn corpus-likely-seq [corpus n]
  (let [transitions (corpus-transitions {} (process-corpus corpus) n)]
    (let [start (rand-nth (vec (keys transitions)))]
      (quite-likely-seq start transitions))))

(defn stringify-seq [seq]
  (->>
   seq
   (replace { "i" "I"})
   (clojure.string/join " ")
   (#(clojure.string/replace % #" +[\.\?,]" (fn [s] (str (second s)))))))

(defn producer [channel running]
  (let [corpus (process-corpus (reddit/reddit-corpus "technology"))]
    (async/go
     (loop [comment-seqs corpus]
       (if @running
         (let []
           (doseq [transition (col-transitions (first comment-seqs) 1)]
             (async/>! channel transition))
           (recur (rest comment-seqs)))
         nil)))))

(defn consumer [channel running redis-conn]
  (async/go
   (while @running
     (let [[from-vec to-symbol] (async/<! channel)]
       (tt/add-transition redis-conn from-vec to-symbol)))))

(def running (atom true))
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [reddit-chan (async/chan)
        redis-conn (RedisConn. nil nil {:app "markovtext" :sr "technology" :n 1})]
    (producer reddit-chan running)
    (consumer reddit-chan running redis-conn)
    (while @running
      (let [msg (read-line)]
        (if (= msg "close")
          (do
            (async/close! reddit-chan)
            (swap! running not)
            (println "closed"))
          (println
           (stringify-seq
            (take 25 (quite-likely-seq (tt/rand-first redis-conn) redis-conn)))))))))
