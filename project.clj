(defproject markovchainz "0.1.0-SNAPSHOT"
  ;:description ""
  ;:url ""
  :main markovchainz.core
  ;:license {:name "Eclipse Public License"
  ;          :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.taoensso/carmine "2.2.0"]
                 [compojure "1.1.5"]
                 [ring "1.2.0"]
                 [http-kit "2.1.10"]
                 [enlive "1.1.4"]
                 [cheshire "5.2.0"]
                 [environ "0.4.0"]]
  :profiles {:uberjar {:aot [markovchainz.core]}}
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler markovchainz.core/handler})
