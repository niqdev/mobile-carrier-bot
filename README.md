# mobile-carrier-bot

A bot to access mobile carrier services implemented in Scala using Tagless Final style

## Endpoints

```
# healt checks
http :8080/status
http :8080/info
http :8080/env
```

## Development

> TODO

```
# test
sbt test -jvm-debug 5005

# run with default
sbt app/run

# run
ENVIRONMENT=local \
TELEGRAM_API_TOKEN=123:xyz \
sbt app/run
```

### sbt aliases

* `checkFormat` checks format
* `format` formats sources
* `update` checks outdated dependencies
* `build` checks format and runs tests

### other sbt plugins

* `dependencyTree` shows project dependencies

TODO

https://github.com/topics/http4s
