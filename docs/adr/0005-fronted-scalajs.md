# 5. Frontend is ScalaJS, one stack for both the query UI and the map

Date: 2026-06-21

## Status

Accepted

## Context

Two frontends are planned: a simple UI to query the existing API (a low-stakes
on-ramp, since the author has not built a UI before), and the eventual
flight-map visualisation. The on-ramp only de-risks the map if both are built in
the **same stack** — otherwise the learning curve is paid twice.

The choice was ScalaJS versus a JS/TS stack (React/Svelte + a map library).
JS/TS has the larger community and a map-native ecosystem. However, this project
is a full-stack-**Scala** learning vehicle; community size is explicitly *not* a
decision factor (if it were, the project would not be in Scala at all).

## Decision

**ScalaJS for both UIs**, same stack throughout. The map will use facades over a
JS map library (Leaflet or MapLibre).

## Consequences

- The domain models and circe codecs in `core` are shared between backend and
  frontend — the API client has **no duplicated DTOs**.
- On-theme with the project's reason to exist (learn the Scala ecosystem
  end-to-end).
- The simple query UI absorbs the ScalaJS learning curve first, so the map phase
  adds only the *map-library* learning, not ScalaJS-the-language. The author's
  own "simple UI first" sequencing is what makes this toolchain tractable.
- Cost: writing facades over JS map libraries, and a smaller community/doc base
  than TS — accepted as irrelevant given the project's goals.

## Alternatives considered

- **TS/React (or Svelte) + a map library.** Larger ecosystem, map-native, gentler
  first step — but off-theme, and it duplicates the domain models on the JS side.
  Rejected because community size is explicitly not a goal here.
