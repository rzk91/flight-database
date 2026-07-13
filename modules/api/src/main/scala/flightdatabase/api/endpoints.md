# Flight Database — API overview

- **Base URL:** `http://localhost:18181/v1/flightdb`
- **Full reference:** [`openapi.yaml`](../../../resources/openapi.yaml), or browse it
  interactively at `http://localhost:18181/v1/flightdb/docs/` once the server is running — a
  per-endpoint reference with request/response schemas and a "try it out" console (see
  _API reference UI_ below).

This file is the at-a-glance map. The OpenAPI spec is the source of truth for the
exact shapes.

---

## Query grammar (applies to every resource)

`{res}` is the resource path segment (e.g. `airports`, `countries`).

| Purpose      | Shape |
|--------------|-------|
| List         | `GET /{res}?return-only={field}&sort-by={field}&order={asc\|desc}&limit={n}&offset={n}` |
| Filter       | `GET /{res}/filter?field={f}&operator={op}&value={v}&sort-by=&order=&limit=&offset=` |
| Sub-filter   | `GET /{res}/{relation}/filter?field={f}&operator={op}&value={v}&…` |
| Single by id | `GET\|HEAD\|PUT\|PATCH\|DELETE /{res}/{id}` |
| Create       | `POST /{res}` |

- **`field`** — any property of the targeted entity (snake_case, e.g. `country_code`).
- **`value`** — single value for most operators; comma-separated list for `in`/`not_in`;
  exactly two comma-separated bounds for `range`.
- A sub-filter targets a field of a **related** entity (e.g. filter countries by a
  language field).

### Operators

`eq` `neq` `is` `is_not` `gt` `gteq` `lt` `lteq` `regex_match` `range`
`in` `not_in` `starts_with` `ends_with` `contains` `not_contains`

Default operator is `eq`. `starts_with`/`ends_with`/`contains`/`not_contains` are
case-insensitive (`ILIKE`); `regex_match` is Postgres `~`.

### Status codes

| Code | Meaning |
|------|---------|
| `200` | OK (a list with no matches still returns `200` with a message) |
| `201` | Created |
| `204` | Deleted |
| `400` | Invalid field / operator / value / id format, or inconsistent ids |
| `404` | Not found |
| `409` | Already exists |
| `422` | SQL / constraint error (e.g. invalid foreign key) |
| `500` | Unknown error |
| `501` | Not implemented |

---

## Resources

Every resource below supports the full standard set
(`HEAD/GET/PUT/PATCH/DELETE /{id}`, `GET /` list, `GET /filter`, `POST /`) unless
noted. "Sub-filters" lists the extra `/{relation}/filter` routes.

| Resource            | Path                 | Sub-filters                              | Notes |
|---------------------|----------------------|------------------------------------------|-------|
| Airlines            | `/airlines`          | `country`                                | |
| Fleet entries       | `/airline-airplanes` | `airline`, `airplane`                    | also `GET /airline/{airline_id}/airplane/{airplane_id}` |
| Hub-city links      | `/airline-cities`    | `airline`, `city`                        | also `GET /airline/{airline_id}/city/{city_id}` |
| Routes              | `/airline-routes`    | `airline`, `airplane`, `airport`         | airport filter takes `inbound`/`outbound` flags |
| Airplane models     | `/airplanes`         | `manufacturer`                           | |
| Airports            | `/airports`          | `city`, `country`                        | |
| Cities              | `/cities`            | `country`                                | |
| Countries           | `/countries`         | `language`, `currency`                   | language filter spans the 3 language slots |
| Currencies          | `/currencies`        | —                                        | |
| Languages           | `/languages`         | —                                        | |
| Manufacturers       | `/manufacturers`     | `city`, `country`                        | |
| Hello-World         | `/hello/{name}`      | —                                        | `GET` only; greeting |

### Examples

```bash
# List, paginated and sorted
curl -i "http://localhost:18181/v1/flightdb/airports?sort-by=capacity&order=desc&limit=10"

# Filter on the resource's own field
curl -i "http://localhost:18181/v1/flightdb/countries/filter?field=country_code&operator=gt&value=40"

# Sub-filter: countries that use English (any language slot)
curl -i "http://localhost:18181/v1/flightdb/countries/language/filter?field=iso2&operator=eq&value=EN"

# Routes out of FRA only
curl -i "http://localhost:18181/v1/flightdb/airline-routes/airport/filter?field=iata&value=FRA&outbound"

# Create
curl -i -X POST http://localhost:18181/v1/flightdb/currencies \
  -H "Content-Type: application/json" \
  -d '{"name": "Japanese Yen", "iso": "JPY", "symbol": "¥"}'
```

---

## Multi-slot filters (any/none)

- `GET /countries/language/filter` (across main/secondary/tertiary) and
  `GET /airline-routes/airport/filter` without `inbound`/`outbound` (across
  start/destination) use **any/none** semantics: a positive operator matches when
  the value is in *any* slot, while an exclusion operator
  (`neq`/`is_not`/`not_in`/`not_contains`) matches only when the value is in
  **no** slot.

---

## API reference UI

`http://localhost:18181/v1/flightdb/docs/` serves an interactive
[Scalar](https://github.com/scalar/scalar) reference for `openapi.yaml`, incl. a "try it
out" console that hits the running server directly. The raw spec is also served on its
own at `http://localhost:18181/v1/flightdb/docs/openapi.yaml`.

The routes are hand-written `HttpRoutes.of` blocks, so the spec is **not generated**
from code — `openapi.yaml` is maintained by hand and must be updated when routes change.
The Scalar UI itself is a pinned `org.webjars.npm:scalar__api-reference` dependency
served from the classpath (see `ApiDocsEndpoints`), not fetched from a CDN, so it works
fully offline once built.

A later, larger alternative is migrating the routes to **tapir**, which would make
them self-describing and generate the OpenAPI spec from code (no hand-maintenance,
no drift) — tracked separately, not part of this change.