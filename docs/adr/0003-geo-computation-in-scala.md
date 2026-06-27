# 3. Geospatial computation lives in application code (Scala), not the database

Date: 2026-06-21

## Status

Accepted

## Context

Three parts of the system need great-circle geometry over airport coordinates:

- the **ETL**, to compute a Route's scheduled In time (block time depends on
  great-circle distance ÷ cruise speed),
- the **simulator**, to compute a Flight's distance *and* its interpolated
  position along the arc, every tick, at runtime,
- a likely future **analytical query layer** (e.g. "routes over 5000 km",
  "long-haul night flights").

The computation could live in the database via a spatial extension
(`earthdistance` or PostGIS) or in a Scala geo-core module.

## Decision

Geospatial math lives in a **Scala geo-core module**:

- **Distance** via the haversine (spherical) formula. Precision is sufficient —
  haversine is roughly 0.5% off the true geodesic, and real flights exceed the
  great circle anyway (airways, winds). No Vincenty/PostGIS-grade geodesy is
  warranted.
- **Position interpolation** along the great-circle arc, for the moving map dot.

The database stores coordinates (per-airport once OurAirports is imported) and
may store *derived* values such as scheduled In or route distance — but those
are computed by the Scala code at import time and persisted as a materialised
derivation, not computed by the database.

## Consequences

- One implementation of the geometry, reused by ETL, simulator, and any future
  analytics. No risk of a Scala result and a SQL result disagreeing.
- The simulator's runtime interpolation — which cannot live in the database — and
  the ETL's distance calculation share the same code path.
- Distance-based API queries are served by persisting a `distance` column
  computed in Scala at import, rather than by adding a spatial extension, so
  Postgres stays vanilla (one less operational dependency).
- If heavy ad-hoc spatial querying ever becomes a first-class need, this is worth
  revisiting (PostGIS) — but not for the current set of needs.

## Alternatives considered

- **PostGIS / `earthdistance` in Postgres.** Powerful spatial SQL, but it
  duplicates math the simulator must have in Scala regardless, adds a database
  extension and ops burden, and still cannot perform the per-tick runtime
  interpolation. Rejected for current needs.
