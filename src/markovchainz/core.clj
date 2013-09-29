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
    font-family: Helvetica Neue;
  }
  #h {
    padding: 0 15px 0;
  }
  #c {
    background: no-repeat center center fixed;
    -webkit-background-size: cover;
    -moz-background-size: cover;
    -o-background-size: cover;
    background-size: cover;
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
                 black -1px -1px 1px,
                 black 1px 1px 2px,
                 black -1px 1px 2px,
                 black 1px -1px 2px,
                 black -1px -1px 2px,
                 black 1.5px 1.5px 2px,
                 black -1.5px 1.5px 2px,
                 black 1.5px -1.5px 2px,
                 black -1.5px -1.5px 2px;
    color: #FFF;
    margin: 3px;
  }
  .permalink {
    padding: 0 15px 15px;
  }
  .error {

  }
  </style>
  <div id='h'>
  <h1>Markov 2 Chainz</h1>
  <p><a href='http://en.wikipedia.org/wiki/Markov_chain'>Generated</a> rap lyrics using songs selected from Mr. Chainz's extensive repertoire.</p>
  <p>Built using Flickr, Rapgenius, Redis, Clojure and <a href='http://clojurecup.com/app.html?app=2chainz'>more</a>.<p>
  <p>A Clojure Cup Entry by <a href='https://twitter.com/dsri'>@dsri</a> and <a href='http://twitter.com/skiaec04'>@skiaec04</a></p>
  </div>
  ")

(defn get-footer []
  "</html>")

(defn get-permalink [id]
  (str (get-header)
    "<p class='permalink'><a href='.'>Generate new</a> | <a href='" id "'>Permalink</a></p>"
    (let [perma (redis/rget id)]
      (if-not (nil? perma)
        (str "<div id='c' style='background-image: url(" (:image perma) ")'><p class='lyrics'><span>" (str/replace (:body perma) #"\n" "</span><br/><span>\n") "</span></p></div>")
        "<p class='error'>Not found</p>"))
    (get-footer)))

(defn get-lyrics []
  (let [body (get-body)]
    (str
      (get-header)
      "<p class='permalink'><a href=''>Regenerate</a> | <a href='" (:key body) "'>Permalink</a></p>"
      "<div id='c' style='background-image: url(" (:image body) ")'><p class='lyrics'><span>" (str/replace (:body body) #"\n" "</span><br/><span>\n") "</span></p></div>"
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
