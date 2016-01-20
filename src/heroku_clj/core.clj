(ns heroku-clj.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.walk :refer [keywordize-keys]]))

(def ^:dynamic *key*
  "Our application API key"
  (atom ""))

(defn set-api-key! [value]
  (reset! *key* value))

(defmacro http>>
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
          (http>> method url api-key))]
    (if (= (class result)
            clojure.lang.PersistentHashMap)
      (keywordize-keys result)
        (map keywordize-keys result))))

(defn full-url [path]
  (str "https://api.heroku.com/" path))

(defn ^:private build-http-request
  "Make a HTTP request and returns it as a hash"
  [key method url query-params]
  (let [full-url (full-url url)]
    { :method method
      :basic-auth ["" key]
      :as :json
      :query-params query-params
      :url full-url }))

(defn do-request
  "Performs a HTTP request to the Heroku API"
  ([type resource key params]
    (let [r (build-http-request key type resource params)]
      (->> (http/request r) :body)))
  ([type resource key]
    (do-request type resource key {})))

(defmacro simple-request
  "A simple get request helper"
  [url key]
  `(do-request :get ~url ~key))

(defn with-key
  "Utility function so that we don't
   need to keep passing around our API key"
  [fn & args]
  (apply (partial fn @*key*) args))

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

(defn app
 ([key]
   (do-request :get "apps" key))
 ([key app]
   (do-request :get (str "apps/" app) key)))

(defn apps [k] (app k))

(defn app-names [k]
  (map :name (apps k)))

(defn app-create
  "Create a new application"
  ([key] (do-request :post "apps" key))
  ([key name] )
  ([key name stack] ))

(defn config
  "List config vars for an app"
  [key app]
  (let [u (format "apps/%s/config_vars" app)
        response (http>> http/get (full-url u) key)]
    (->> (json/parse-string response)
         (keywordize-keys))))

(defn collaborators
  "List collaborators for an app"
  [key app]
  (simple-request (format "apps/%s/collaborators" app) key))

(defn domains
  "List domains for an app"
  [key app]
  (simple-request (format "apps/%s/domains" app) key))

(defn domain-names [key app]
  (into []
    (map :domain (domains key app))))

(defn keys
  "List SSH keys for an app"
 [key]
 (simple-request "/user/keys" key))

(defn list-processes
  "List processes for an app"
  [key app]
  (simple-request (format "apps/%s/ps" app) key))

(defn app-status
  "Is our application still running?"
  [key app]
  (->> (list-processes key app)
       (map (juxt :app_name :state))
       (map (fn [[k _]] [(keyword k) _]))
       (into {})))

(defn releases
  "List releases for an app"
  [key app]
  (simple-request (format "apps/%s/releases" app) key))

(defn stacks
  "List all available stacks for an app"
  [key app]
  (simple-request (format "apps/%s/stack" app) key))

(defn current-stack
  "Show the current stack running for an app"
  [key app]
  (->> (stacks key app)
       (filter #(true? (get % :current)))
       first
       :name))
