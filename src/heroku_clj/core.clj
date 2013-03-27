(ns heroku-clj.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def ^:dynamic key
  (atom ""))

(defn set-api-key! [value]
  (reset! key value))

(defn symbolize-keys
  "Take all string keys and turn them into symbols"
  [m]
  (into {}
    (for [[k v] m]
      [(keyword k) v])))

(defmacro request->>
  "Make a generic request with required params
   - url the request url
   - api-key your api key
   - request-fn a http/get or http/post etc"
  [request-fn url api-key & params]
  `(let [auth-params# {:basic-auth ["" ~api-key]}]
     (when-let
       [response# (~request-fn ~url
                    (merge {:accept :json}
                            auth-params#))]
       (get response# :body))))

(defn request
  "Make a request to the Heroku API
   - url the request url
   - api-key your api key"
  [url api-key]
  (let [result
        (json/parse-string
          (request->> http/get url api-key))]
    (if (= (class result)
            clojure.lang.PersistentHashMap)
      (symbolize-keys result)
      (map symbolize-keys result))))

(defn full-url [path]
  (str "https://api.heroku.com/" path))

(defn do-request
  "Performs a request and handles any exceptions"
  [resource key]
  (let [u (full-url resource)]
    (try
      (with-meta
        (request u key)
        {:url u :key key})
    (catch Exception e
      (print (.getMessage e))))))

;; Apps

(defn app
 ([key]
   (do-request "apps" key))
 ([key app]
   (do-request (str "apps/" app) key)))

(defn app-create
  "Create a new application"
  ([])
  ([name])
  ([name stack]))

;; Processes

(defn list-processes
  "List processes for an app"
  [key app]
  (do-request (format "apps/%s/ps" app) key))

