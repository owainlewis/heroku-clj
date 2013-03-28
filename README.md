# heroku-clj

The Heroku API is useful and awesome. This is a Clojure client for accessing the API.

## Usage

All requests require an API key and return a response as JSON.

Your API key can be found under account settings on Heroku.

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

## License

Copyright © 2013 Owain Lewis

Distributed under the Eclipse Public License, the same as Clojure.

