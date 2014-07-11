(ns markovtext.cli
  (:gen-class)
  (:require
   [clojure.string :as string]))

(defn process-args [& args]
  {:opts (into #{} (for [opt args :when (= \- (first opt))] (string/replace opt #"-+" "")))
   :args (filter (comp #(not= % \-) first) args)})
