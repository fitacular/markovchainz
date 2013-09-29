(ns markovchainz.core
  (:require
    [clojure.string :as str]
    [markovchainz.text-generator :as gen]
    [markovchainz.redis :as redis]
    [markovchainz.flickr :as flickr]
    [compojure.route :as route])
  (:use [environ.core :only [env]])
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
  (str "<!DOCTYPE html><html>
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
    padding: 12px 15px 0;
  }
  #c {
    background: no-repeat center center fixed;
    -webkit-background-size: cover;
    -moz-background-size: cover;
    -o-background-size: cover;
    background-size: cover;
    padding: 5%;
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
  td p {
    margin: 0;
    line-height: 150%;
  }
  </style>
  <title>Markov 2 Chainz</title>
  <div id='h'>
  <table border='0'>
  <tr><td style='padding-right: 25px;'><h1>Markov 2 Chainz</h1></td>
  <td><p>Lyrics are generated dynamically using a <a href='https://en.wikipedia.org/wiki/Markov_chain'>Markov chain</a> from a corpus of songs from <a href='https://twitter.com/2chainz'>Mr. Chainz</a>'s extensive repertoire.</p>
  <p>Built with Clojure, Redis, Flickr, Rapgenius, and <a href='http://clojurecup.com/app.html?app=2chainz'>more</a>.<p>
  <p>A Clojure Cup entry by <a href='https://twitter.com/dsri'>@dsri</a> and <a href='https://twitter.com/skiaec04'>@skiaec04</a>.</p>
  </td></tr></table>
  </div>
  "))

(defn get-footer []
  "</html>\n\n")

(defn get-permalink [id]
  (let [perma (redis/rget id)]
    (str
      (get-header)
      "<p class='permalink'><a href='.'>Generate new lyrics</a> | <a href='" id "'>Permalink to these lyrics</a> | <a href='" (:image perma) "'>Flickr image source</a></p>"
      (if-not (nil? perma)
        (str "<div id='c' style='background-image: url(" (:image perma) ")'><p class='lyrics'><span>" (str/replace (:body perma) #"\n" "</span><br/><span>\n") "</span></p></div>")
        "<h2>Not found</h2>")
      (get-footer))))

(defn get-lyrics []
  (let [body (get-body)]
    (str
      (get-header)
      "<p class='permalink'><a href=''>Generate new lyrics</a> | <a href='" (:key body) "'>Permalink to these lyrics</a> | <a href='" (:image body) "'>Flickr image source</a></p>"
      "<div id='c' style='background-image: url(" (:image body) ")'><p class='lyrics'><span>" (str/replace (:body body) #"\n" "</span><br/><span>\n") "</span></p></div>"
      (get-footer))
    ))

(defroutes handler
  (GET "/" []
    (get-lyrics))
  (GET "/:id" [id]
    (get-permalink id))
  (ANY "*" []
    {:status 404, :body "<!DOCTYPE html>\n<html><h2>Not found</h2></html>\n"}))

(def app
  (-> (var handler)
    (wrap-reload '(ring-tutorial.core))
    (wrap-stacktrace)))

(defn boot []
  (run-jetty #'app {:port (read-string (or (env :port) "80"))}))

(defn -main []
  (boot))
