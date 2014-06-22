(ns markovtext.redis
  (:gen-class)
  (:require
   clojure.core
   [taoensso.carmine :as car :refer (wcar)]))

(def server-conn {:pool nil :spec nil})

(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn redis-get
  ([key] (wcar* (car/get {:app "markovtext" :key key})))
  ([key default]
     (let [result (redis-get key)]
       (if (nil? result) default result))))

(defn redis-set! [key val]
  (wcar* (car/set {:app "markovtext" :key key} val)))

(defn update-transition! [key list]
  (let [curr-transition (redis-get key '())]
    (redis-set! key (concat curr-transition list))))

(defn update-transitions! [col-trans]
  (for [[k li] col-trans]
    (update-transition! k li)))

