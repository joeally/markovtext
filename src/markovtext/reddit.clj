ns markovtext.reddit
  (:gen-class)
  (:require
   [clj-http.client :as client]
   [clojure.string :as string])
  (:import [java.net URLEncoder]))

(def redditurl "http://www.reddit.com")

(defn format-data [data]
  (->> data
       ;; Convert keys and values to suitable form
       (map (fn [[k v]] [(name k) (URLEncoder/encode (str v))]))
       ;; Join up pairs with "="
       (map #(string/join #"=" %))
       ;; Join the kv pairs
       (string/join #"&")))


(defn urlpost [url data cookie]
  (let [response
    (client/post url
                 {:headers {"User-Agent" "reddit.clj"}
                  :cookies cookie
                  :content-type "application/x-www-form-urlencoded"
                  :body (format-data data)
                  :as :json
                  :socket-timeout 10000
                  :conn-timeout 10000})]
    (if (= 200 (:status response)) response)))

(defn login [user passwd]
  "Login to reddit"
  (let [resp (urlpost
              (str "http://www.reddit.com/api/login/" user)
              {:user user :passwd passwd :api_type "json"} nil)
        cookie (:cookies resp)
        result (:body resp)
        resultmap (:json result)]
    (if (empty? (:errors resultmap))
      {:modhash (:modhash (:data resultmap)) :cookies cookie})))

(defn urlopen [url data cookie]
  (let [response (client/get (string/join [url "?" (format-data data)])
                             {:headers {"User-Agent" "reddit.clj"}
                              :cookies cookie
                              :as :json
                              :socket-timeout 10000
                              :conn-timeout 10000})]
    (if (= 200 (:status response))
      (:body response)
      nil)))

(defn get-listing [url after]
  (->> (urlopen url {:limit 1000} nil)
       (:data)))

(defn get-listings
  ([url after]
     (Thread/sleep 2000)
     (let [listing (get-listing url after)]
       (lazy-cat (:children listing) (get-listings url (:after listing)))))
  ([url]
     (let [listing (get-listing url "")]
       (lazy-cat (:children listing) (get-listings url (:after listing))))
     ))

(defn process-reddit [st]
  (-> st
      (string/replace #"\((http://)?[\w\.\?&]+\)" "")
      (string/replace #"[\*\(\)\[\]]" "")
      (string/replace #"\d\)" "")))

(defn sr-comments [sr]
  (map
   :data
   (get-listings (string/join [redditurl "/r/" sr "/comments.json"]))))

(defn reddit-corpus [sr]
  (map
   (comp process-reddit :body)
   (sr-comments sr)))

