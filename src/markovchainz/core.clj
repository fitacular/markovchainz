(ns markovchainz.core
  (:require
    [clojure.string :as str]
    [markovchainz.text-generator :as text-generator])
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace))

(defn get-stanza []
  (let [lines 5]
    (str (str/join "\n" (repeatedly lines #(text-generator/line))) "\n")))

(defn get-body []
  (let [stanzas 3]
    (str (str/join "\n" (repeatedly stanzas #(get-stanza))) "\n")))

(defn handler [req]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    (get-body)})

(def app
  (-> #'handler
    (wrap-reload '(ring-tutorial.core))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app {:port 8080}))

(defn -main []
  (boot))
