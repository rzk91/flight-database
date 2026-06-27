# 4. The flight feed is an in-process simulator, not an external feed

Date: 2026-06-20

## Status

Accepted

*(Documented after ADR-0001..0003, though the decision was taken earlier in the
design discussion — it is the foundation the others build on.)*

## Context

The project's chosen direction is to turn the static Route catalogue into a
runtime feed of in-flight activity, visualised on a map. That feed could come
from:

- a **real external source** (a flight-data API such as FlightAware or
  AeroDataBox, or scraping a flight-tracker site), or
- an **in-process simulator** that generates flight activity from the Route
  graph already in the database, driven by a controllable simulation clock.

The governing constraints are the project's, not a product's: it is a
learning vehicle, explicitly **not** a public service; there is no budget for
paid feeds; and a core stated learning goal is practising **stateful stream
processing with recovery**, which is far easier against a deterministic,
replayable stream than a live one.

## Decision

The feed is an **in-process simulator**. It reads the persisted Route graph and
emits flight events against a sim-clock that can be sped up, paused, and
replayed. There is no external feed.

## Consequences

- **Self-contained:** no external dependency, no cost, no rate limits, no terms-
  of-service exposure.
- **Deterministic and replayable:** ideal for practising stateful processing and
  crash recovery — you can replay the exact same stream.
- **Synthetic:** timings, delays, and cancellations are generated, not real. This
  is why a temporal model and a geo-core had to be built (see ADR-0001..0003).
- A future **"real-data mode"** — swapping the simulator's output for a
  legitimate schedule API (e.g. AeroDataBox's freemium tier) behind the same
  event interface — remains possible but is explicitly out of scope now.

## Alternatives considered

- **External live feed.** Realistic, but costly, an external dependency, rate-
  limited, and not replayable on demand — it directly contradicts the
  self-containment and recovery-practice goals. Rejected.
- **Web scraper for schedules/positions.** Brittle (markup changes, bot
  defences), usually against site terms, high maintenance for a payoff the
  generated data already provides. Rejected.
