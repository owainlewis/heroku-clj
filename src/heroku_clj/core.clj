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
  [method url api-key]
  (let [result
        (json/parse-string
          (request->> method url api-key))]
    (if (= (class result)
            clojure.lang.PersistentHashMap)
      (symbolize-keys result)
      (map symbolize-keys result))))

(defn full-url [path]
  (str "https://api.heroku.com/" path))

(defn do-request
  "Performs a request and handles any exceptions"
  [type resource key]
  (let [u (full-url resource)
        req-fn (condp = type
                :get  http/get
                :post http/post
                :else http/get)]
    (try
      (with-meta
        (request req-fn u key)
        {:url u :key key})
    (catch Exception e
      (print (.getMessage e))))))

(defmacro simple-request [url key]
  `(do-request :get ~url ~key))

;; Addons

(defn addons
  "List all availible add ons"
  [key]
  (->> (do-request :get "addons" key)
       (map :name)))

(defn list-addons
  "List all addons for an app"
  [key app]
  (do-request :get
    (format "apps/%s/addons" app) key))

(defn create-addon [key app addon]
  (do-request :post
    (format "apps/%s/addons/%s" app addon) key))

;; Apps

(defn app
 ([key]
   (do-request :get "apps" key))
 ([key app]
   (do-request :get (str "apps/" app) key)))

(defn apps [k] (app k))

(defn app-create
  "Create a new application"
  ([])
  ([name])
  ([name stack]))

;; Config

(defn config
  "List config vars for an app"
  [key app]
  (let [u (format "apps/%s/config_vars" app)
        response (request->> http/get (full-url u) key)]
    (->> (json/parse-string response)
         (symbolize-keys))))

;; Collaborators

(defn collaborators
  "List collaborators for an app"
  [key app]
  (simple-request (format "apps/%s/collaborators" app) key))

;; Domains

(defn domains
  "List domains for an app"
  [key app]
  (simple-request (format "apps/%s/domains" app) key))

(defn domain-names [key app]
  (into []
    (map :domain (domains key app))))

;; Keys

(defn keys
  "List SSH keys for an app"
 [key]
 (simple-request "/user/keys" key))

;; Processes

(defn list-processes
  "List processes for an app"
  [key app]
  (simple-request (format "apps/%s/ps" app) key))

(defn app-status
  "Is our application still running?"
  [key app]
  (map (juxt :state :app_name)
    (list-processes key app)))

;; Releases

(defn releases
  "List releases for an app"
  [key app]
  (simple-request (format "apps/%s/releases" app) key))

;; Stacks

(defn stacks
  "List all available stacks for an app"
  [key app]
  (simple-request (format "apps/%s/stack" app) key))

(defn current-stack
  "Show the current stack running for an app"
  [key app]
  (->> (stacks key app)
       (filter (fn [m]
         (= true (get m :current))))
       first
       :name))

