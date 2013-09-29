(ns markovchainz.core
  (:require
    [clojure.string :as str]
    [markovchainz.text-generator :as gen]
    [markovchainz.redis :as redis]
    [markovchainz.flickr :as flickr]
    [compojure.route :as route])
  (:use compojure.core)
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace))


(defn get-body []
  (let [body-text (gen/body)
        image (flickr/get-image)
        key (redis/get-random-key)
        response {:body body-text :image image :key key}]
    (redis/rset key response)
    response))

(defn get-header []
  "<!DOCTYPE html><html>
  <style type='text/css'>
  html {
    overflow-y:scroll;
    overflow:-moz-scrollbars-vertical;
  }
  body {
    padding: 0;
    margin: 0;
  }
  #h {
    font-family: Helvetica;
  }
  #c {
    background: url(https://farm9.staticflickr.com/8460/7943814380_0846ee8feb_z.jpg);
    background-size: 120%;
    padding: 5%;
  }
  #f {

  }
  .lyrics {
    font-variant: small-caps;
    font-size: 20pt;
    font-family: Georgia;
  }
  .lyrics span {
    text-shadow: black 1px 1px 1px,
                 black -1px 1px 1px,
                 black 1px -1px 1px,
                 black -1px -1px 1px;
    color: #FFF;
    margin: 3px;
  }
  .permalink {

  }
  .error {

  }
  </style>
  <div id='h'><h1>Markov 2 Chainz</h1></div>
  ")

(defn get-footer []
  "</html>")

(defn get-permalink [id]
  (str (get-header)
    "<p class='permalink'><a href='.'>Generate new</a> | <a href='" id "'>Permalink</a></p>"
    (let [perma (redis/rget id)]
      (if-not (nil? perma)
        (str "<div id='c' style='background: url(" (:image perma) ")'><p class='lyrics'><span>" (str/replace (:body perma) #"\n" "</span><br/><span>\n") "</span></p></div>")
        "<p class='error'>Not found</p>"))
    (get-footer)))

(defn get-lyrics []
  (let [body (get-body)]
    (str
      (get-header)
      "<p class='permalink'><a href=''>Regenerate</a> | <a href='" (:key body) "'>Permalink</a></p>"
      "<div id='c' style='background: url(" (:image body) ")'><p class='lyrics'><span>" (str/replace (:body body) #"\n" "</span><br/><span>\n") "</span></p></div>"
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
