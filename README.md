# mobile-carrier-bot

A bot to access mobile carrier services implemented with the Typelevel stack using Tagless Final style

> Heavy Work in Progress :warning:

:construction::construction::construction::construction::construction::construction::construction::construction::construction::construction:

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

### Sbt aliases

* `checkFormat` checks format
* `format` formats sources
* `update` checks outdated dependencies
* `build` checks format and runs tests

### Other sbt plugins

* `dependencyTree` shows project dependencies
