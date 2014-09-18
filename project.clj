(defproject markovtext "0.1.0-SNAPSHOT"
  :description "An exploration into markov chains with clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [org.clojure/tools.trace "0.7.5"]
                 [com.gfredericks/debug-repl "0.0.1"]
                 [org.clojure/tools.trace "0.7.5"]
                 [clj-http "0.9.2"]
                 [com.taoensso/carmine "2.6.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [incanter "1.2.3-SNAPSHOT"]]
  :main ^:skip-aot markovtext.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
