# 1. Schedule is persisted on Route; Flight runtime state is generated, never persisted

Date: 2026-06-20

## Status

Accepted

## Context

The project is adding a runtime feed of in-flight activity, driven by an
in-process **simulator** rather than an external flight feed (no external
dependency, no cost, fully replayable — see the simulator direction). The
simulator must turn the static Route catalogue into moving Flights.

This requires a temporal model that does not exist today: Routes are stored as
endpoints plus a route number, with no notion of when anything departs or how
long it takes. When we introduced the temporal model, two distinct kinds of
time-related data surfaced, and they have opposite lifecycles:

- **Schedule** — *when a Route is intended to depart.* Recurring and stable.
  This is what makes Emirates EK45 and EK47 (both DXB→FRA) different Routes
  rather than duplicates.
- **Flight runtime state** — *where a given Flight is right now, and whether it
  is on time.* Exists only while a simulation session is running; meaningless
  outside the system clock.

The question was whether the whole temporal model is persisted into the schema,
generated at runtime, or split.

## Decision

Split it along ownership:

- The **Route owns the Schedule**, and the Schedule is **persisted** (a
  departure-time column on `airline_route`; a cruise-speed column on `airplane`
  to derive duration is anticipated but not yet decided).
- The **Flight owns its runtime state** (position, percent complete, delay), and
  that state is **generated at runtime and never persisted**.

This matches the existing glossary: a Route is "static recurring intent" and a
Flight "lives in the feed, not the schedule."

## Consequences

- Departure time becomes queryable through the existing CRUD/filter API for free,
  because it is just another Route column.
- The simulator can iterate on position interpolation, delays, and clock speed
  without schema migrations, because none of that is persisted.
- Restarting a simulation produces a clean slate (no stale Flights to reconcile).
  Session-scoped recovery of *aggregate* state is a separate, later concern and
  is explicitly out of scope here.
- Adding the departure-time column is a migration and a backfill of the 42 seed
  Routes; reversing the persist/generate split later would be costly, which is
  why this is recorded.

## Alternatives considered

- **Persist everything** (including positions/delays): maximally queryable and
  "realistic," but couples the schema to a model still in heavy flux and forces a
  migration on every experiment. Rejected at this stage.
- **Generate everything** (including departure times from a runtime seed):
  fully self-contained, but throws away the natural, already-correct home for
  departure time (the Route) and makes the schedule unqueryable. Rejected.
