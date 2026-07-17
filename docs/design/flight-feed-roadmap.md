# Flight feed & visualisation — design note and roadmap

A working design note for the project's chosen direction: turning the static
aviation catalogue into a **simulated live flight feed** with a **ScalaJS**
visualisation. Learning-driven, not a public service. This note is the map of
the plan; the *why* behind each hard decision lives in the ADRs it references.

## The shape

```
Postgres (Routes + airport coords + schedule)
   │   ETL (fs2, one-off / re-runnable)
   ▼
Simulator  ── emits FlightEvents against a sim-clock ──►  Kafka topic
   │                                                         │
   │                                          fs2 processor (live-flight state)
   │                                                         │
   │                                              http4s push (WebSocket/SSE)
   │                                                         ▼
   └───────────────────────────────────────────►  ScalaJS map
```

The geo core (Scala: haversine distance + great-circle interpolation) lives in
`domain` — a `flightdatabase.geo` package, not a separate sbt module — and is
shared by the ETL (to compute durations) and the simulator (distance + the
moving dot). One implementation, three consumers: ETL, simulator, and a future
analytics layer.

## Current module layout

The backend was restructured (PR #15) into seven modules with clean separation
of concerns. New flight-feed modules slot in without touching the existing
domain or API layers.

| Module | Contents |
|---|---|
| `domain` | Entities, algebras, `FieldType` GADT, `ApiError`, geo core (`flightdatabase.geo`: haversine distance + great-circle interpolation). **No doobie dependency.** |
| `persistence` | Repositories, doobie queries, `DatabaseConfig`, Flyway schema migrations, doobie syntax (`FieldType → Put/Read` bridge). Seed data lives with `persistence-it` until ETL (#28/#29) can populate the catalogue. |
| `api` | http4s endpoints, `ApiConfig`/`ApiLogging`, http4s + enum-string syntax, endpoint unit tests. |
| `app` | `FlightDbMain`, `Server`, aggregate `Configuration`, pureconfig loader. Composition root. |
| `syntax` | Pure, domain-independent generic extensions (formerly `utils`). |
| `testkit` | Shared test-support helpers (`matchers`, `ioresult`, `response`) under `flightdatabase.test.syntax`. |
| `persistence-it` | Repository and query integration tests (formerly `backend-it`). Requires Docker. |

Planned additions: `simulator`, `etl`, `frontend` (ScalaJS), and a Kafka processor. Geo core is not a new module — it lands in `domain` (see above).

## Domain model additions

See `CONTEXT.md` for the canonical definitions. In brief:

- **Flight** — a runtime instance of a Route, keyed `(Route, date)`, born when the
  sim-clock reaches the Route's scheduled Out. Carries ephemeral state; never
  persisted.
- **Flight lifecycle (OOOI)** — Out (off-block) → Off (wheels up) → On (wheels
  down) → In (on-block). Taxi-out = Out→Off, taxi-in = On→In. Cancellation can
  occur before Out (ground-delay cap) or during taxi-out; never once airborne.
- **Schedule** — persisted intent on the Route: scheduled Out (STD), optionally
  scheduled In (STA). Distinct from the actual OOOI a Flight achieves.

Temporal model (all terms now have a home):

```
scheduled In = scheduled Out + taxi-out + (great-circle distance ÷ cruise_speed) + taxi-in
```

(Add a fixed climb/descent allowance later if tighter block times are wanted.)

## Decisions in force

- **ADR-0001** — Schedule persisted on Route; Flight runtime state generated, never persisted.
- **ADR-0002** — Deterministic catalogue facts (cruise_speed, taxi times) persisted; stochastic behaviour (delay/cancellation) in code.
- **ADR-0003** — Geospatial computation in Scala, not the database.
- **ADR-0004** — The feed is an in-process simulator, not an external feed.
- **ADR-0005** — Frontend in ScalaJS, one stack for the query UI and the map.

## Roadmap

### Phase 0 — backend groundwork
- ~~Module restructure~~ ✓ — domain is doobie-free (`FieldType` GADT); persistence /
  api / app separated; `syntax` and `testkit` extracted. Seven clean modules.
- Schema migration: scheduled Out (and decide store-vs-derive for scheduled In),
  `cruise_speed` on `airplane`, taxi-out/taxi-in times on `airport`, and **lat/long
  on `airport`** (today coordinates live only on `city`).
- Geo core: haversine distance + great-circle position interpolation, as a
  `flightdatabase.geo` package in `domain` (pure, no doobie — domain is already
  dependency-free at that tier, and both the ETL and the simulator will depend
  on `domain` anyway for `Route`/`Airport`). Unit-testable without a DB.
- fs2 ETL: import a chosen **subset of hub airports** + the routes among them, from
  OurAirports (airports + coordinates, CC0) and OpenFlights / curated data
  (routes; accepted as somewhat stale). Schedule times: use a supplied time if the
  input row has one, else generate a **seeded** plausible one (stable across
  re-imports). Re-runnable against a clean DB.

### Phase 1 — UI on-ramp
- A simple **ScalaJS** UI over the *existing* API: pick an entity, set filters, see
  results. Depends on nothing in Phase 0, so it can start immediately. Purpose:
  earliest payoff, and absorb the ScalaJS learning curve before the map.

### Phase 2 — backend simulator
- Sim-clock (controllable rate; single repeating day to start).
- FlightEvent generation across the OOOI lifecycle, with delay/cancellation
  stochastics (in code).
- Kafka topic for flight events; fs2 processor folding events into the live-flight
  map (keyed `(Route, date)`).
- http4s push (WebSocket or SSE) of live state to the frontend.

### Phase 3 — frontend map
- ScalaJS map via a facade over Leaflet/MapLibre. Render a flight only from **Out**;
  airport badge for "departing soon". Great-circle arcs; moving dot via geo-core
  interpolation; hover popup for flight detail; status by colour/icon.

### Banked for later (second trunk, not now)
- Aggregate live state (per-airport / per-route), and the persistence + recovery
  practice that lives there.
- Analytical query layer over the catalogue (distance-based filters, "long-haul",
  "night flight" — terms to sharpen *when* this opens; note `city.timezone` anchors
  "night").
- An LLM free-text → API-query capstone, on top of a then-rich query API.
- Optional "real-data mode" via a legitimate schedule API behind the event interface.

## Open todos
- Airport coordinates (solved by the OurAirports import) and multi-airport cities
  (e.g. LHR/LGW/STN) no longer sharing a city centroid.
- Decide: store scheduled In, or derive it on demand.
- Hub subset selection for the first import.
- Climb/descent allowance in block time, if/when tighter realism is wanted.
