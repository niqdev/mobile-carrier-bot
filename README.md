# mobile-carrier-bot

A bot to access mobile carrier services implemented on top of the Typelevel stack using Tagless Final style

---

:construction::construction::construction::construction::construction::construction::construction::construction::construction::construction:
:warning: **Heavy Work in Progress** :warning:
:construction::construction::construction::construction::construction::construction::construction::construction::construction::construction:

*TODO (not in order):*

- [x] skeleton, plugins, setup
- [ ] architecture docs and diagrams
- [x] healtcheck status/info/env
- [ ] expose prometheus metrics via endpoint
- [ ] expose JVM metrics via JMX
- [ ] scalatest and scalacheck
- [ ] codecov or alternatives
- [x] telegram client (polling)
- [ ] slack client (webhook)
- [x] scrape at least 2 mobile carrier services to check balance
- [ ] (polling) notify for low credits and expiry date
- [x] in-memory db with Ref
- [ ] doobie db with PostgreSQL and H2
- [ ] if/how store credentials in a safe way
- [ ] authenticated endpoints as alternative to telegram/slack
- [ ] write pure FP lib alternative to scala-scraper and jsoup (I will never do this!)
- [ ] fix scalastyle and scalafmt
- [ ] [slate](https://lord.github.io/slate) static site for api
- [ ] [gitpitch](https://gitpitch.com) for 5@4 presentation
- [ ] constrain all types with refined where possible
- [ ] travis
- [ ] publish to dockerhub
- [ ] create simple deployment k8s chart + argocd app
- [ ] statefulset with PostgreSQL
- [ ] alerting with prometheus to slack
- [ ] grafana dashboard
- [ ] backup/restore logs and metrics even if re-create cluster
- [ ] generate and publish scaladoc
- [ ] fix manual Circe codecs with withSnakeCaseMemberNames config
- [ ] add gatling stress tests
- [ ] add integration tests

## Endpoints

```
# healt checks
http :8080/status
http :8080/info
http :8080/env
```

## Development

```bash
# test
sbt test -jvm-debug 5005

# run with default
TELEGRAM_API_TOKEN=123:xyz sbt app/run
```

### sbt aliases

* `checkFormat` checks format
* `format` formats sources
* `update` checks outdated dependencies
* `build` checks format and runs tests

### Other sbt plugins

* `dependencyTree` shows project dependencies

## Deployment

```bash
# build image
sbt clean docker:publishLocal

# run temporary container
docker run \
  --rm \
  --name mobile-carrier-bot \
  niqdev/mobile-carrier-bot-app:0.1

# access container
docker exec -it mobile-carrier-bot bash
```
