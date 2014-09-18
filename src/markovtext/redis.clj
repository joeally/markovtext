(ns markovtext.redis
  (:gen-class)
  (:require
   [markovtext.TransitionTable :as tt]
   [taoensso.carmine :as car :refer (wcar)]))

(defrecord RedisConn [pool spec key-options])

(def server-conn {:pool nil :spec nil})

(defmacro wcar* [server-conn & body] `(car/wcar ~server-conn ~@body))

(comment "We are going to store keys of the transition table as normal redis keys.

Each key will correspond to a redis set of transitions. Previously we've constructed a multiset (also known as a bag) but redis doesn't have these so we will need to adapt our use of the set.

Each value we are transitioning to will be prepended with a count which we will increment each time a new transition is added to the transition table. This way we can have multiple instances of the same word in our transition table

So each item in the set will be of the form transition-to:count")

(def lua-add-string 
  (str "return redis.call('sadd', KEYS[1], string.format('%s:%d', KEYS[2], redis.call('incr', 'markovcount')))"))

(defn- gen-key [key key-options]
  (merge key-options {:key key}))

(defn- parse-key [key]
  (:key key))

(extend-protocol tt/TransitionTable
  RedisConn
  (rand-next [tt prev]
    (-> (wcar* tt (car/srandmember (gen-key prev (:key-options tt))))
        (clojure.string/split #":")
        (first)))
  (rand-first [tt]
    (parse-key (wcar* tt (car/randomkey))))
  (add-transition [tt from to]
    (try 
      (wcar* tt (car/eval lua-add-string 2 (gen-key from (:key-options tt)) to))
      (catch Exception e
        (println "ERROR [from]:" from to "hello")
        (println (.getMessage e))))
    tt))
