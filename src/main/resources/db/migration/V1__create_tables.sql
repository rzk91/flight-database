-- Create flight database (this is done manually)
-- CREATE DATABASE flights;

DROP TABLE IF EXISTS fleet_route;
DROP TABLE IF EXISTS fleet_airplane;
DROP TABLE IF EXISTS fleet;
DROP TABLE IF EXISTS manufacturer;
DROP TABLE IF EXISTS airplane;
DROP TABLE IF EXISTS airport;
DROP TABLE IF EXISTS city;
DROP TABLE IF EXISTS country;
DROP TABLE IF EXISTS currency;
DROP TABLE IF EXISTS language;

-- Create language table
CREATE TABLE language (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	iso2 character varying (2) NOT NULL CHECK (btrim(iso2::text) <> ''::text),
	iso3 character varying (3) CHECK (btrim(iso3::text) <> ''::text),
	original_name character varying CHECK (btrim(original_name::text) <> ''::text),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create currency table
CREATE TABLE currency (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	iso character varying NOT NULL CHECK (btrim(iso::text) <> ''::text),
	symbol character varying (3),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create country table
CREATE TABLE country (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	iso2 character varying (2) NOT NULL CHECK (btrim(iso2::text) <> ''::text),
	iso3 character varying (3) NOT NULL CHECK (btrim(iso3::text) <> ''::text),
	country_code integer NOT NULL CHECK (country_code > 0),
	domain_name character varying (10),
	main_language_id integer REFERENCES language (id) ON DELETE CASCADE,
	secondary_language_id integer REFERENCES language (id) ON DELETE CASCADE,
	tertiary_language_id integer REFERENCES language (id) ON DELETE CASCADE,
	currency_id integer REFERENCES currency (id) ON DELETE CASCADE,
	nationality character varying NOT NULL CHECK (btrim(nationality::text) <> ''::text),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create city table
CREATE TABLE city (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	country_id integer REFERENCES country (id) ON DELETE CASCADE,
	capital boolean NOT NULL,
	population integer NOT NULL CHECK (population > 0),
	latitude numeric NOT NULL,
	longitude numeric NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create manufacturer table
CREATE TABLE manufacturer (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	city_based_in integer REFERENCES city (id) ON DELETE CASCADE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airport table
CREATE TABLE airport (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	icao character varying (4) NOT NULL CHECK (btrim(icao::text) <> ''::text),
	iata character varying (3) NOT NULL CHECK (btrim(iata::text) <> ''::text),
	city_id integer REFERENCES city (id) ON DELETE CASCADE,
	number_of_runways integer NOT NULL CHECK (number_of_runways > 0),
	number_of_terminals integer NOT NULL CHECK (number_of_terminals > 0),
	capacity integer CHECK (capacity > 0),
	international boolean NOT NULL,
	junction boolean NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet table
CREATE TABLE fleet (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	iso2 character varying (2) NOT NULL CHECK (btrim(iso2::text) <> ''::text),
	iso3 character varying (3) NOT NULL CHECK (btrim(iso3::text) <> ''::text),
	call_sign character varying,
	hub_airport_id integer REFERENCES airport (id) ON DELETE CASCADE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airplane table
CREATE TABLE airplane (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL CHECK (btrim(name::text) <> ''::text),
	manufacturer_id integer REFERENCES manufacturer (id) ON DELETE CASCADE,
	capacity integer NOT NULL CHECK (capacity > 0),
	max_range_in_km integer NOT NULL CHECK (max_range_in_km > 0),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet-airplane table
CREATE TABLE fleet_airplane (
	id serial NOT NULL,
	PRIMARY KEY (id),
	fleet_id integer REFERENCES fleet (id) ON DELETE CASCADE,
	airplane_id integer REFERENCES airplane (id) ON DELETE CASCADE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet-route table
CREATE TABLE fleet_route (
	id serial NOT NULL,
	PRIMARY KEY (id),
	fleet_airplane_id integer REFERENCES fleet_airplane (id) ON DELETE CASCADE,
	route_number character varying (10) NOT NULL CHECK (btrim(route_number::text) <> ''::text),
	start_airport_id integer REFERENCES airport (id) ON DELETE CASCADE,
	destination_airport_id integer REFERENCES airport (id) ON DELETE CASCADE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);