# Flight Database

[![CI](https://github.com/rzk91/flight-database/actions/workflows/ci.yml/badge.svg)](https://github.com/rzk91/flight-database/actions/workflows/ci.yml)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

A Scala backend modelling the static aviation world — airlines, airports, airplane models (with
manufacturers), countries, cities, and the scheduled routes between them — exposed over an HTTP
API. Built with [http4s](http://http4s.org/), [doobie](http://tpolecat.github.io/doobie/), and
[circe](https://circe.github.io/circe/) on PostgreSQL, with schema managed by
[Flyway](https://flywaydb.org/), and a seeded, comprehensive set of entries in the database.

> Work in progress, and a learning project rather than a production service. Current direction:
> layering a **simulated** live flight feed on top of the existing catalogue (see
> [ADR-0004](docs/adr/0004-simulator-not-external-feed.md)) — not a connection to any real
> flight-data provider.

## Modules

The codebase is a modular monolith of seven sbt modules (see
[the restructure rationale](docs/design/restructure-plan.md)):

| Module | Contents |
|---|---|
| `domain` | Entities, algebras, `FieldType` GADT, `ApiError`, geo core (haversine distance + great-circle interpolation). No doobie dependency. |
| `persistence` | Repositories, doobie queries, `DatabaseConfig`, Flyway migrations + seed data. |
| `api` | http4s endpoints, `ApiConfig`/`ApiLogging`, endpoint unit tests. |
| `app` | `FlightDbMain`, `Server`, aggregate configuration, pureconfig loader — the composition root. |
| `syntax` | Pure, domain-independent generic extensions. |
| `testkit` | Shared test-support helpers. |
| `persistence-it` | Repository/query integration tests (needs Docker — uses testcontainers). |

## Documentation map

- [`CONTEXT.md`](CONTEXT.md) — domain glossary (Route vs. Flight, Schedule, Fleet, OOOI lifecycle, etc.)
- [`docs/erd.md`](docs/erd.md) — entity-relationship diagram of the database schema
- [`docs/adr/`](docs/adr) — architecture decisions (why the feed is simulated, why geo math lives in Scala, why the frontend will be ScalaJS, etc.)
- [`docs/design/flight-feed-roadmap.md`](docs/design/flight-feed-roadmap.md) — the detailed phased roadmap
- [`modules/api/src/main/scala/flightdatabase/api/endpoints.md`](modules/api/src/main/scala/flightdatabase/api/endpoints.md) — API endpoint reference
- [`HELP.md`](HELP.md) — Docker / psql / curl cheat sheet for local development

## Requirements
- sbt 2.0.0
- JDK 21
- PostgreSQL 16 (Docker recommended — see [`HELP.md`](HELP.md))

## Quick start
1. `cp docker/.env.example docker/.env` and `docker compose -f docker/docker-compose.yml up -d`
   to start Postgres with the `flightdb` database already created — see [`HELP.md`](HELP.md) for
   the full Docker/psql command reference.
2. `set -a && source docker/.env && set +a` to put the matching credentials in your shell, then
   `sbt app/run` — this runs the Flyway migrations (incl. seed data) and starts the API at
   `http://localhost:18181/v1/flightdb`.
3. Browse the interactive API reference at `http://localhost:18181/v1/flightdb/docs/`, explore
   with the curl examples in [`HELP.md`](HELP.md), or read the endpoint reference linked above.

## Roadmap

Currently in **Phase 0** (backend groundwork): the module restructure is done, and schema support
for schedules, `cruise_speed`, taxi times and airport coordinates has landed. Next up:

- **Phase 1** — a ScalaJS UI over the existing query API (earliest payoff, no Phase-0 dependency)
- **Phase 2** — an in-process flight simulator emitting `FlightEvent`s over Kafka, folded into
  live-flight state by an fs2 processor
- **Phase 3** — a ScalaJS map (Leaflet/MapLibre) rendering flights live from the feed
- Banked for later: aggregate live-state persistence/recovery, an analytical query layer over the
  catalogue, and an LLM free-text → API-query capstone

See [`docs/design/flight-feed-roadmap.md`](docs/design/flight-feed-roadmap.md) for the full plan,
decisions in force, and open todos.
