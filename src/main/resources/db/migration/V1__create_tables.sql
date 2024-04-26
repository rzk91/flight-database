-- Create flight database (this is done manually)
-- CREATE DATABASE <db-name>;

DROP TABLE IF EXISTS fleet_route;
DROP TABLE IF EXISTS fleet_airplane;
DROP TABLE IF EXISTS fleet;
DROP TABLE IF EXISTS airline_route;
DROP TABLE IF EXISTS airline_city;
DROP TABLE IF EXISTS airline_airplane;
DROP TABLE IF EXISTS airline;
DROP TABLE IF EXISTS manufacturer;
DROP TABLE IF EXISTS airplane;
DROP TABLE IF EXISTS airport;
DROP TABLE IF EXISTS city;
DROP TABLE IF EXISTS country;
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS language;

-- Create language table
CREATE TABLE language
(
    id              bigserial            NOT NULL,
    PRIMARY KEY (id),
    name            character varying    NOT NULL CHECK (btrim(name::text) <> ''::text),
    iso2            character varying(2) NOT NULL CHECK (btrim(iso2::text) <> ''::text),
    iso3            character varying(3) CHECK (btrim(iso3::text) <> ''::text),
    original_name   character varying    NOT NULL CHECK (btrim(original_name::text) <> ''::text),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create currency table
CREATE TABLE currency
(
    id              bigserial         NOT NULL,
    PRIMARY KEY (id),
    name            character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
    iso             character varying NOT NULL CHECK (btrim(iso::text) <> ''::text),
    symbol          character varying(3),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create country table
CREATE TABLE country
(
    id                    bigserial            NOT NULL,
    PRIMARY KEY (id),
    name                  character varying    NOT NULL CHECK (btrim(name::text) <> ''::text),
    iso2                  character varying(2) NOT NULL CHECK (btrim(iso2::text) <> ''::text),
    iso3                  character varying(3) NOT NULL CHECK (btrim(iso3::text) <> ''::text),
    country_code          integer              NOT NULL CHECK (country_code > 0),
    domain_name           character varying(10),
    main_language_id      bigint               NOT NULL REFERENCES language (id) ON DELETE CASCADE,
    secondary_language_id bigint REFERENCES language (id) ON DELETE CASCADE,
    tertiary_language_id  bigint REFERENCES language (id) ON DELETE CASCADE,
    currency_id           bigint               NOT NULL REFERENCES currency (id) ON DELETE CASCADE,
    nationality           character varying    NOT NULL CHECK (btrim(nationality::text) <> ''::text),
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create city table
CREATE TABLE city
(
    id              bigserial         NOT NULL,
    PRIMARY KEY (id),
    name            character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
    country_id      bigint            NOT NULL REFERENCES country (id) ON DELETE CASCADE,
    capital         boolean           NOT NULL,
    population      bigint            NOT NULL CHECK (population > 0),
    latitude        numeric           NOT NULL,
    longitude       numeric           NOT NULL,
    timezone        character varying NOT NULL CHECK (btrim(timezone::text) <> ''::text),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create manufacturer table
CREATE TABLE manufacturer
(
    id              bigserial         NOT NULL,
    PRIMARY KEY (id),
    name            character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
    city_based_in   bigint            NOT NULL REFERENCES city (id) ON DELETE CASCADE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airport table
CREATE TABLE airport
(
    id                  bigserial            NOT NULL,
    PRIMARY KEY (id),
    name                character varying    NOT NULL CHECK (btrim(name::text) <> ''::text),
    icao                character varying(4) NOT NULL CHECK (btrim(icao::text) <> ''::text),
    iata                character varying(3) NOT NULL CHECK (btrim(iata::text) <> ''::text),
    city_id             bigint               NOT NULL REFERENCES city (id) ON DELETE CASCADE,
    number_of_runways   integer              NOT NULL CHECK (number_of_runways > 0),
    number_of_terminals integer              NOT NULL CHECK (number_of_terminals > 0),
    capacity            bigint               NOT NULL CHECK (capacity > 0),
    international       boolean              NOT NULL,
    junction            boolean              NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airline table
CREATE TABLE airline
(
    id              bigserial            NOT NULL,
    PRIMARY KEY (id),
    name            character varying    NOT NULL CHECK (btrim(name::text) <> ''::text),
    iata            character varying(2) NOT NULL CHECK (btrim(iata::text) <> ''::text),
    icao            character varying(3) NOT NULL CHECK (btrim(icao::text) <> ''::text),
    call_sign       character varying    NOT NULL CHECK (btrim(call_sign::text) <> ''::text),
    country_id      bigint               NOT NULL REFERENCES country (id) ON DELETE CASCADE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airplane table
CREATE TABLE airplane
(
    id              bigserial         NOT NULL,
    PRIMARY KEY (id),
    name            character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
    manufacturer_id bigint            NOT NULL REFERENCES manufacturer (id) ON DELETE CASCADE,
    capacity        integer           NOT NULL CHECK (capacity > 0),
    max_range_in_km integer           NOT NULL CHECK (max_range_in_km > 0),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airline-airplane table
CREATE TABLE airline_airplane
(
    id              bigserial NOT NULL,
    PRIMARY KEY (id),
    airline_id      bigint    NOT NULL REFERENCES airline (id) ON DELETE CASCADE,
    airplane_id     bigint    NOT NULL REFERENCES airplane (id) ON DELETE CASCADE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airline-city table
CREATE TABLE airline_city
(
    id              bigserial NOT NULL,
    PRIMARY KEY (id),
    airline_id      bigint    NOT NULL REFERENCES airline (id) ON DELETE CASCADE,
    city_id         bigint    NOT NULL REFERENCES city (id) ON DELETE CASCADE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airline-route table
CREATE TABLE airline_route
(
    id                     bigserial             NOT NULL,
    PRIMARY KEY (id),
    airline_airplane_id    bigint                NOT NULL REFERENCES airline_airplane (id) ON DELETE CASCADE,
    route_number           character varying(10) NOT NULL CHECK (btrim(route_number::text) <> ''::text),
    start_airport_id       bigint                NOT NULL REFERENCES airport (id) ON DELETE CASCADE,
    destination_airport_id bigint                NOT NULL REFERENCES airport (id) ON DELETE CASCADE,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);