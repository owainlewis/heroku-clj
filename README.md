# heroku-clj

The Heroku API is useful and awesome. This is a Clojure client for accessing the API.

## Usage

All requests require an API key and return a response as JSON.

Your API key can be found under account settings on Heroku.

## End points

+ Addons
+ Apps
+ Collaborators
+ Config
+ Domains
+ Keys
+ Logs
+ Processes
+ Releases
+ Stacks

### Addons

List all addons (only returns the addon name by default)

```clojure
(addons "YOURAPIKEY")
```

List addons for an application

```clojure
(addons "YOURAPIKEY" "YOURAPP")
```

Create a new addon

```clojure
(create-addon "YOURAPIKEY" "YOURAPP" "dbinsights:basic")
```

### Apps

List all your apps on Heroku

```clojure
(app "YOURAPIKEY")
```

Show info for a single app

```clojure
(app "YOURAPIKEY" "myapp")
```

Create a new app

```clojure
(create-app "myapp" "cedar")
```

### Processes

Show process information for an app

```clojure
(list-processes "YOURAPIKEY" "myapp")
```

## Alternative DSL

If you don't want to pass an API key to every function you can use with-key to make things easier

First you need to set your api-key globally.

```clojure
(set-api-key! "YOURKEY")
```
Then you can use with-key to call functions without having to pass in the api key

```clojure
(with-key apps "forwardio")
```

## License

Copyright © 2013 Owain Lewis

Distributed under the Eclipse Public License, the same as Clojure.

