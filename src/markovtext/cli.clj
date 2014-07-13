(ns markovtext.cli
  (:gen-class)
  (:require
   [clojure.string :as string]))

(defn process-args [& args]
  {:single-opts (filter (partial re-matches #"^-\w+") args)
   :double-opts (filter (partial re-matches #"^--\w+") args)
   :args (filter (partial re-matches #"^\w+") args)})
