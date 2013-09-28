(ns markovchainz.scrape
  [:require
   [org.httpkit [client :as http]]
   [net.cgrand.enlive-html :as html]]
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

(defn yuck []
  (get-lyrics-blob "http://lyrics.wikia.com/2_Chainz:Yuck!"))
