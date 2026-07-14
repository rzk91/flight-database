# Some useful commands

## Docker commands
- Copy the example env file (holds the Postgres credentials shared by Compose and the app) and
  adjust it if needed  
`cp docker/.env.example docker/.env`
- Start PostgreSQL in the background, creating the `flightdb` database and `docker` role from
  `docker/.env`  
`docker compose -f docker/docker-compose.yml up -d`
- Export the same credentials into your shell so `sbt app/run` (see below) can pick them up  
`set -a && source docker/.env && set +a`
- Running `FlightDbMain` from an IDE instead of the CLI? The IDE launches the JVM directly, so it
  won't inherit vars exported in a shell — add `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`, and
  `DB_BASE_URL` (same values as `docker/.env`) to the run configuration's environment variables
  instead.
- Jump into the PostgreSQL container's default database  
`docker compose -f docker/docker-compose.yml exec postgres psql -U docker`
- Jump into the PostgreSQL container to the database "flightdb"  (only possible if the database "flightdb" already exists)  
`docker compose -f docker/docker-compose.yml exec postgres psql -U docker -d flightdb`
- Stop PostgreSQL (data persists in the `flightdb-data` volume)  
`docker compose -f docker/docker-compose.yml down`

## psql commands
- List all databases  
`\l`
- Switch to database "flightdb"  
`\c flightdb`
- List all relations  
`\d`
- List all tables  
`\dt`
- Describe table name "airport" (or any relation for that matter — table names are singular)  
`\d airport`
- List all users  
`\du`
- Show help  
`\h`

## cURL commands
> Full endpoint reference, incl. the general query grammar (filtering, sorting, pagination):
> [`endpoints.md`](modules/api/src/main/scala/flightdatabase/api/endpoints.md)

- Simple API GET request with `-i` to include HTTP header info  
`curl -i http://localhost:18181/v1/flightdb/hello/rahul`
- Fetch the raw OpenAPI spec (or open `http://localhost:18181/v1/flightdb/docs/` in a browser for
  the interactive Scalar reference, incl. "try it out")  
`curl -s http://localhost:18181/v1/flightdb/docs/openapi.yaml`
- Get currencies and pretty print JSON output (`-s` suppresses any other output)  
`curl -s http://localhost:18181/v1/flightdb/currencies | jq .` 
- Get airplanes of a certain manufacturer (e.g. Airbus), via the `manufacturer` sub-filter  
`curl -s "http://localhost:18181/v1/flightdb/airplanes/manufacturer/filter?field=name&value=Airbus" | jq .`
- Get only the names of cities in a certain country (e.g. Germany): filter by the `country`
  sub-filter, then pick out `.name` client-side (filter and `return-only` can't be combined)  
`curl -s "http://localhost:18181/v1/flightdb/cities/country/filter?field=name&value=Germany" | jq '.[].name'`
- Get only the `name` field for every city, using `return-only`  
`curl -s "http://localhost:18181/v1/flightdb/cities?return-only=name" | jq .`
- Post a new language with JSON input  
```sh
curl -X POST -H "Content-Type: application/json" \
     -d '{"name": "LANGUAGE_NAME", "iso2": "LN", "iso3": "LNA", "original_name": "Original_Language_Name"}' \
     http://localhost:18181/v1/flightdb/languages
```
