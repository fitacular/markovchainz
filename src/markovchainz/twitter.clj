(ns markovchainz.twitter
  [:require
    [markovchainz.text-generator :as gen]
    [clojure.string :as str]
    [http.async.client :as ac]]
  [:use
    [environ.core :only [env]]
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.restful]]
  [:import
    (twitter.callbacks.protocols SyncSingleCallback)])

(def my-creds (make-oauth-creds (env :twitter-m2c-app-consumer-key)
                                (env :twitter-m2c-app-consumer-secret)
                                (env :twitter-m2c-user-access-token)
                                (env :twitter-m2c-user-access-token-secret)))

(defn get-tweet
  ([]
    (let [new-tweet (gen/line)]
      (if (< 140 (count new-tweet))
        (get-tweet)
        (get-tweet new-tweet)
        )))
  ([base]
    (let [new-tweet (str/join " / " [base (gen/line)])]
      (if (< 140 (count new-tweet))
        base
        (get-tweet new-tweet)
      ))))

(defn -main []
  (with-open [client (ac/create-client :request-timeout -1 :follow-redirect false)]
    (statuses-update :oauth-creds my-creds
                     :client client
                     :params { :status (get-tweet) })))
