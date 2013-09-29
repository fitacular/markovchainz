(ns markovchainz.flickr
  [:require
   [org.httpkit [client :as http]]
   [cheshire.core :as json]
   [clojure.string :as string]]
   [:use [environ.core :only [env]]]
  [:import
   [java.io ByteArrayInputStream]])

(def api-key (env :flickr-access-key))

(defn search-for-keyword
  [keyword]
  (let [resp (http/get "http://api.flickr.com/services/rest/"
                       {:query-params {:api_key api-key
                                       :method "flickr.photos.search"
                                       :tags keyword
                                       :license "1,2,4,5"
                                       :format "json"
                                       :per_page 20
                                       :page (rand-nth (range 1 10))}})]
    (json/parse-string
     (string/replace (:body @resp) #"^jsonFlickrApi\(|\)$" "")
     true)))

(defn build-flickr-url [{:keys [farm server secret id] :as photo}]
  (str "http://farm" farm ".staticflickr.com/" server "/" id "_" secret ".jpg"))

(defn get-images []
  (let [keyword "kitten"
        {{photos :photo} :photos} (search-for-keyword keyword)]
    (map build-flickr-url photos)))


(defn get-image []
  (try (rand-nth (get-images))
       (catch Exception e "http://farm4.staticflickr.com/3773/9904361105_3302581b52.jpg")))
