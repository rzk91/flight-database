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
- Create a new database named "flightdb" (should be executed before running `FlightDbMain`)  
`CREATE DATABASE flightdb;`
- List all databases  
`\l`
- List all relations  
`\d`
- List all tables  
`\dt`
- Describe table name "airports" (or any relation for that matter)  
`\d airports`
- List all users  
`\du`
- Switch to database "flightdb"  
`\c flightdb`
- Show help  
`\h`

## cURL commands
- Simple API GET request with `-i` to include HTTP header info  
`curl -i http://localhost:18181/hello/me`
- Get currencies and pretty print JSON output (`-s` suppresses any other output)  
`curl -s http://localhost:18181/flightdb/currencies | jq .` 
- Post a new language with JSON input  
```sh
curl -X POST -H "Content-Type: application/json" \
     -d '{"name": "LANGUAGE_NAME", "iso2": "LN", "iso3": "LNA", "original_name": "Original_Language_Name"}' \
     http://localhost:18181/flightdb/languages
```
