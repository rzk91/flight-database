# Some useful commands

## Docker commands
- Pull required PostgreSQL image (currently, 16)  
`docker pull postgres:16`
- Run PostgreSQL in the background and perform port forwarding to 5432  
`docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 postgres:16`
- Jump into the PostgreSQL container to the default database  
`docker exec -it postgres psql -U postgres`
- Jump into the PostgreSQL container to the database "flightdb"  (only possible if the database "flightdb" already exists)
`docker exec -it postgres psql -U postgres -d flightdb`

## psql commands
- `CREATE DATABASE flightdb;` (should be executed before running `FlightDbMain`)
- `\h` (help)
- `\l` (list all databases)
- `\d` (list all relations)
- `\dt` (list all tables)
- `\d {table_name}` (describe table_name)
- `\du` (list all users)
- `\c {database_name}` (switch to database_name)

## cURL commands
- Simple API GET request with `-i` to include HTTP header info  
`curl -i http://localhost:18181/hello/me`
- Get currencies and pretty print JSON output (suppress any other output using `-s`)  
`curl -s http://localhost:18181/flightdb/currencies | jq .` 
- Post a new language with JSON input
```sh
curl -X POST -H "Content-Type: application/json" \
     -d '{"name": "LANGUAGE_NAME", "iso2": "LN", "iso3": "LNA", "original_name": "Original_Language_Name"}' \
     http://localhost:18181/flightdb/languages
```
