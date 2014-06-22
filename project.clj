(defproject markovtext "0.1.0-SNAPSHOT"
  :description "An exploration into markov chains with clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.2"]
                 [com.taoensso/carmine "2.6.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [incanter "1.2.3-SNAPSHOT"]]
  :main ^:skip-aot markovtext.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
