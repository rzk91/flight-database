# Entity-Relationship Diagram

Generated from the schema in
[`V1__create_tables.sql`](../modules/persistence/src/main/resources/db/migration/V1__create_tables.sql)
plus later migrations (airport coordinates/taxi times, airplane cruise speed). Update this
diagram whenever a migration adds/renames/removes a table or column.

Every table also has `created_at` and `last_updated_at` timestamp columns (omitted below for
brevity — see [`V2__create_update_triggers.sql`](../modules/persistence/src/main/resources/db/migration/V2__create_update_triggers.sql)
for the triggers that maintain them).

```mermaid
erDiagram
    LANGUAGE {
        bigint id PK
        varchar name
        varchar iso2
        varchar iso3
        varchar original_name
    }

    CURRENCY {
        bigint id PK
        varchar name
        varchar iso
        varchar symbol
    }

    COUNTRY {
        bigint id PK
        varchar name
        varchar iso2
        varchar iso3
        int country_code
        varchar domain_name
        bigint main_language_id FK
        bigint secondary_language_id FK
        bigint tertiary_language_id FK
        bigint currency_id FK
        varchar nationality
    }

    CITY {
        bigint id PK
        varchar name
        bigint country_id FK
        boolean capital
        bigint population
        numeric latitude
        numeric longitude
        varchar timezone
    }

    MANUFACTURER {
        bigint id PK
        varchar name
        bigint base_city_id FK
    }

    AIRPORT {
        bigint id PK
        varchar name
        varchar icao
        varchar iata
        bigint city_id FK
        int number_of_runways
        int number_of_terminals
        bigint capacity
        boolean international
        boolean junction
        numeric latitude
        numeric longitude
        int taxi_out_duration
        int taxi_in_duration
    }

    AIRLINE {
        bigint id PK
        varchar name
        varchar iata
        varchar icao
        varchar call_sign
        bigint country_id FK
    }

    AIRPLANE {
        bigint id PK
        varchar name
        bigint manufacturer_id FK
        int capacity
        int max_range_in_km
        int cruise_speed
    }

    AIRLINE_AIRPLANE {
        bigint id PK
        bigint airline_id FK
        bigint airplane_id FK
    }

    AIRLINE_CITY {
        bigint id PK
        bigint airline_id FK
        bigint city_id FK
    }

    AIRLINE_ROUTE {
        bigint id PK
        bigint airline_airplane_id FK
        varchar route_number
        bigint start_airport_id FK
        bigint destination_airport_id FK
    }

    LANGUAGE ||--o{ COUNTRY : "is main language of"
    LANGUAGE o|--o{ COUNTRY : "is secondary language of"
    LANGUAGE o|--o{ COUNTRY : "is tertiary language of"
    CURRENCY ||--o{ COUNTRY : "is used by"
    COUNTRY ||--o{ CITY : "has"
    COUNTRY ||--o{ AIRLINE : "is home to"
    CITY ||--o{ MANUFACTURER : "is base of"
    CITY ||--o{ AIRPORT : "has"
    MANUFACTURER ||--o{ AIRPLANE : "produces"
    AIRLINE ||--o{ AIRLINE_AIRPLANE : "operates"
    AIRPLANE ||--o{ AIRLINE_AIRPLANE : "is operated as"
    AIRLINE ||--o{ AIRLINE_CITY : "has hub in"
    CITY ||--o{ AIRLINE_CITY : "is hub for"
    AIRLINE_AIRPLANE ||--o{ AIRLINE_ROUTE : "flies"
    AIRPORT ||--o{ AIRLINE_ROUTE : "is origin of"
    AIRPORT ||--o{ AIRLINE_ROUTE : "is destination of"
```
