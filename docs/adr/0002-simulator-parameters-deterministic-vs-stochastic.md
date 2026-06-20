# 2. Simulator parameters: deterministic catalogue facts are persisted; stochastic behaviour stays in code

Date: 2026-06-21

## Status

Accepted (refines ADR-0001)

## Context

ADR-0001 split *Schedule* (persisted, owned by Route) from *Flight runtime
state* (generated, never persisted). Building the simulator surfaced a third
category that ADR-0001 did not address: the **input parameters** the simulator
needs to turn a Route into believable motion —

- cruise speed (to derive airborne duration from great-circle distance),
- typical taxi-out / taxi-in durations,
- the delay distribution and the cancellation probability / cap.

The question was whether all of these become persisted columns (the initial
preference, justified by a clean-DB-reset dev workflow that makes migrations
cheap) or stay in code.

## Decision

Split them by whether they are a fact about a domain entity or a knob of the
simulation:

- **Deterministic, entity-attributable catalogue facts are persisted as
  columns.** `cruise_speed` on `airplane` (this resolves the open item left by
  ADR-0001), and typical taxi-out / taxi-in times on `airport`. These are real
  properties a domain expert would recognise, and they become queryable through
  the existing API for free.
- **Stochastic simulation behaviour stays in code/config, never the schema.**
  Delay distributions, cancellation probability, and the delay cap are
  properties of the *simulator*, not of any Airline, Airport, Route, or
  Airplane. A distribution has no row to be an attribute of.

Rule of thumb: if a domain expert would recognise it as a fact about an
aircraft or an airport, it is a column; if it is a knob of the simulation, it is
code.

## Consequences

- `cruise_speed` and taxi times read as genuine catalogue data and are queryable
  via the existing CRUD/filter endpoints.
- The schema and `CONTEXT.md` stay free of simulation-engine vocabulary, keeping
  the ubiquitous language clean.
- Retuning the random behaviour (the part that changes most during grass-roots
  iteration) needs no migration at all. Persisted-parameter churn is also cheap
  for now because the dev workflow is clean-reset-on-start rather than
  incremental migration — which means ADR-0001's "iterate without migrations"
  consequence is now achieved by the workflow, not by avoiding persistence.
- A future move of a parameter across the boundary (e.g. promoting a per-airport
  delay mean to a column) is a deliberate, reviewable change rather than an
  accident.

## Alternatives considered

- **Persist everything, including delay/cancellation parameters.** No natural
  column home for a distribution; would force invented columns and couple the
  schema to the simulator. Rejected.
- **Keep cruise speed and taxi times in code too.** Loses queryability and the
  chance to treat them as the real catalogue facts they are. Rejected.
