(ns markovchainz.core
  (:require
    [clojure.string :as str]
    [markovchainz.text-generator :as gen]
    [markovchainz.redis :as redis]
    [compojure.route :as route])
  (:use compojure.core)
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace))

(defn get-header []
  "<!DOCTYPE html><html>")

(defn get-footer []
  "</html>")

(defn get-permalink [id]
  (str (get-header)
    (let [perma (redis/rget id)]
      (if-not (nil? perma)
        (str "<p style='font-variant: small-caps; font-size: 24pt'>" (str/replace perma #"\n" "<br/>\n") "</p>")
        "<p>Not found</p>"))
    (get-footer)))

(defn get-lyrics []
  (let [body (gen/get-body)]
    (str
      (get-header)
      "<p style='font-variant: small-caps; font-size: 24pt'>" (str/replace (:body body) #"\n" "<br/>\n") "</p>"
      "<p><a href='" (:key body) "'>Permalink</a></p>"
      (get-footer))
    ))

(defroutes handler
  (GET "/" []
    (get-lyrics))
  (GET "/:id" [id]
    (get-permalink id))
  (ANY "*" []
    {:status 404, :body "<html><h1>Not found</h1></html>"}))

(def app
  (-> (var handler)
    (wrap-reload '(ring-tutorial.core))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app {:port 8080}))

(defn -main []
  (boot))
