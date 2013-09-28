(ns markovchainz.core
  (:require
    [markovchainz.text-generator :as text-generator])
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace))

(defn handler [req]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "hello, world!\n"})

(def app
  (-> #'handler
    (wrap-reload '(ring-tutorial.core))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app {:port 8080}))

(defn -main []
  (boot)
  )
