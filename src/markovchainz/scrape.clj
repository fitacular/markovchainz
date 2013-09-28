(ns markovchainz.scrape
  [:require
   [org.httpkit [client :as http]]
   [net.cgrand.enlive-html :as html]
   [markovchainz [redis :as redis]]]
  [:import
   [java.io ByteArrayInputStream]])

(defn html-string-to-enlive
  [html]
  (let [bytes (ByteArrayInputStream. (.getBytes html "UTF-8"))]
    (html/html-resource bytes)))

(defn get-lyrics-blob
  [url]
  (:content (first (html/select
                    (html-string-to-enlive (:body @(http/get url)))
                    [:.lyricbox]))))

(defn extract-words [blob]
  (filter #(not (map? %)) blob))

(defn get-title [url]
  (:content (first (html/select
                    (html-string-to-enlive (:body @(http/get url)))
                    [:.WikiaPageHeader :> :h1]))))

(defn extract-title [blob]
  (last (clojure.string/split
         (first blob) #":")))

(defn song-map [url]
  (let [lyrics (extract-words (get-lyrics-blob url))
        title (extract-title (get-title url))]
  {:title title :lyrics lyrics :url url}))


(defn yuck []
  (song-map "http://lyrics.wikia.com/2_Chainz:Yuck!"))

(defn birthday []
  (song-map "http://lyrics.wikia.com/2_Chainz:Birthday_Song"))

(defn no-lie []
  (song-map "http://lyrics.wikia.com/2_Chainz:No_Lie"))

(defn save-songs
  ([urls] (map #(redis/add-to-set "songs" (song-map %)) urls))
 ([] (save-songs ["http://lyrics.wikia.com/2_Chainz:Yuck!" "http://lyrics.wikia.com/2_Chainz:Birthday_Song" "http://lyrics.wikia.com/2_Chainz:No_Lie"])))
