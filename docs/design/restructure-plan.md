# Module restructuring — plan (v2)

A working plan for splitting the monolithic `backend` module and purifying the
foundational modules, ahead of the ScalaJS frontend (see `flight-feed-roadmap.md`).

## Target module graph

```
domain          → (no internal deps)   circe, enumeratum [+doobie-core until step 5]
syntax          → (no internal deps)   cats-core, commons-io          ← pure, domain-independent
testkit         → domain               scalatest, scalactic, http4s-core (→ cats-effect, cats-core)
persistence     → domain, syntax       doobie, flyway, pureconfig, logging, icu
api             → domain, syntax       http4s(server/dsl/circe), enumeratum   [+ testkit % Test]
app             → api, persistence      pureconfig, ember-server, logging + slf4j-runtime
persistence-it  → persistence           testcontainers, doobie-scalatest       [+ testkit % Test]
frontend        → (future, ScalaJS)
```

Key properties:
- **`api ⊥ persistence`** — neither depends on the other; `app` is the composition root that wires them.
- **`domain` and `syntax` are independent foundational siblings** — neither depends on the other.
- `domain` is persistence/transport-free after step 5.
- `syntax` is cross-compilable in principle (pure of cross-module coupling; fully JS-pure once `path` leaves).

## Package conventions

- `app` owns the bare roots: `flightdatabase` (entrypoint) and `flightdatabase.config` (assembled `Config`).
- Module code is module-qualified: `flightdatabase.api.*`, `flightdatabase.persistence.*`.
- Syntax/extension namespaces, all parallel:
  - `flightdatabase.syntax` — generic (`double`, `iterable`, `option`, `try_`, `path`, generic `string`)
  - `flightdatabase.persistence.syntax` — doobie (`connectionio`, `query`, `update`, `sqlstate`)
  - `flightdatabase.api.syntax` — `kleisli` + `toOrder`/`toOperator`
  - `flightdatabase.test.syntax` — `matchers`, `ioresult`, `response` (module `testkit`)
- Config slices: `flightdatabase.persistence.config` (`DatabaseConfig` + loader), `flightdatabase.api.config`
  (`ApiConfig`), `flightdatabase.config` (`Config` + `Environment` + loader, in `app`).
- Each syntax package has an **orthogonal** `all` aggregator (generic ⊕ module-specific are NOT
  re-exported into each other; a file wanting both imports both). Individual `object`s
  (`string`, `kleisli`, …) remain importable.

## Steps

### Step 1 — `core → domain` ✅ committed (`a53f5dd`)
Module path + sbt name only; package stayed `flightdatabase.*`.

### Step 2 — Split `backend` → `persistence` + `api` + `app`  ✅ done
- Moves: `repository/**`, `db/**` → persistence; `api/**` → api; `FlightDbMain`, `Server.scala`,
  config aggregate → app.
- `Server.scala` → `app` (api yields `HttpApp`; app runs it).
- **`Repositories[F]` algebra bundle in `domain`**; `FlightDbApi` takes `Repositories[F]`
  (drop `RepositoryContainer` import + the dead `implicit Transactor`); `RepositoryContainer extends
  Repositories[F]` (one-liner — repos already extend their algebras).
- **Config split**: `DatabaseConfig`+`Access`+loader → persistence; `ApiConfig`+`ApiLogging` → api;
  `Config`+`Environment`+loader → app.
- **Per-module `reference.conf`**: `db-config{}` → persistence, `api-config{}` → api, `env` → app.
- `backend/src/test/**` → `api/src/test/**`; repoint `backend-it` → persistence.

### Step 3 — `backend-it → persistence-it`  ✅ done
- Repoint to `persistence`; `PostgreSqlContainerSpec` uses `DatabaseConfig.loadUnsafe` instead of
  `Configuration.configUnsafe.dbConfig` (drops http4s from the IT path).

### Step 4 — Distribute `utils`, rename → `syntax`  ✅ done
- Rename module `utils → syntax`; package `flightdatabase.extensions → flightdatabase.syntax`.
- doobie ext (`connectionio`, `query`, `update`, `sqlstate`) → `persistence` (`flightdatabase.persistence.syntax`).
- http4s `kleisli` → `api` (`flightdatabase.api.syntax`).
- test ext (`matchers`, `ioresult`, `response`) → **`testkit`** (`flightdatabase.test.syntax`; ScalaTest as
  a compile dep), consumed by `api` + `persistence-it` test scopes via `testkit % Test`.
- **Split `string`**: generic stays in `syntax`; `toOrder`/`toOperator` → `api.syntax`.
  ⇒ `syntax` **drops `dependsOn(domain)`** and `enumeratum`.
- `path` stays in `syntax` for now (JVM-only; leaves with the future ETL/feed module).
- `all.scala` → per-module orthogonal `all` aggregators.

### Step 5 — Purge doobie from `domain` (`FieldType[V]`)  ⬜ not started (next)
- `FieldType` → `FieldType[V]` GADT; partial traits take `FieldType[V]` explicitly instead of `Put`/`Read`.
- `FieldFixture`: drop `Put`/`Read`, keep `Decoder`, promote `fieldType` to `FieldType[V]`.
- `Endpoints`: `f[String](…)` → `f(…, StringType)`.
- `putFor`/`readFor` bridge in `persistence`; drop `doobie-core` from `domain`.

## Cross-cutting decisions
- cats-**core** allowed in `domain`/`syntax`; cats-**effect** banned from both.
- circe stays in `domain` (pure; inline `@ConfiguredJsonCodec`).
- Cross-compile (`crossProject` + Scala.js) deferred to frontend kickoff; `path` + `commons-io` exit
  `syntax` then (→ ETL/feed module).
- `build.sbt`: granular dependency groups; 7-module skeleton (`domain`, `syntax`, `testkit`,
  `persistence`, `api`, `app`, `persistence-it`).

## Status

Steps 1–4 complete. `sbt clean Test/compile` is green across all 7 modules
(domain, syntax, testkit, persistence, api, app, persistence-it) — main + tests.

Notable details from the migration:
- `domain` already holds `Repositories[F]` (algebra bundle); `RepositoryContainer extends Repositories[F]`;
  `FlightDbApi` takes `Repositories[F]` + `api.config.ApiConfig` (no `RepositoryContainer`/`Transactor`).
  ⇒ `api` does not depend on `persistence`.
- Config split: `persistence.config.DatabaseConfig` (+ `load`/`loadUnsafe`), `api.config.ApiConfig`,
  `flightdatabase.config.Configuration` (`Config` + loader, in `app`).
- Package-object/parent-scope gotcha: files moved a level deeper lost unqualified access to
  `flightdatabase.*` members (`ApiResult`, `TableBase`, `circeConfig`, …) and needed explicit imports
  (`repository/package.scala`, `InvalidFlightDbObject`).
- Per-module `reference.conf`: `db-config`→persistence, `api-config`→api, `env`→app. Flyway migrations +
  JSON seed moved to `persistence/src/main/resources/db`. `log4j.properties`→app. IT `application.conf`→persistence-it.
- `PostgreSqlContainerSpec` now uses `DatabaseConfig.loadUnsafe` (drops http4s from the IT path).

### Caveat to verify when running (not compile-checked)
- The IT `application.conf` (`modules/persistence-it/src/test/resources`) sets `db-config.url` directly,
  whereas `DatabaseConfig` expects `base-url` + `db-name` (with a derived `url`). This predates the
  restructure; confirm `DatabaseConfig.loadUnsafe` resolves it (pureconfig unknown-key handling) when
  running the ITs under Docker.

### Step 5 (remaining)
`FieldType[V]` GADT + drop doobie from `domain` + `putFor`/`readFor` bridge in `persistence`. Not started.