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
	name character varying NOT NULL,
	iso2 character varying (2) NOT NULL,
	iso3 character varying (3),
	original_name character varying,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create currency table
CREATE TABLE currency (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	iso character varying NOT NULL,
	symbol character varying (3),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create country table
CREATE TABLE country (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	iso2 character varying (2) NOT NULL,
	iso3 character varying (3) NOT NULL,
	country_code integer NOT NULL,
	domain_name character varying (10),
	main_language_id integer REFERENCES language (id),
	secondary_language_id integer REFERENCES language (id),
	tertiary_language_id integer REFERENCES language (id),
	currency_id integer REFERENCES currency (id),
	nationality character varying NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create city table
CREATE TABLE city (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	country_id integer REFERENCES country (id) NOT NULL,
	capital boolean NOT NULL,
	population integer NOT NULL,
	latitude numeric NOT NULL,
	longitude numeric NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create manufacturer table
CREATE TABLE manufacturer (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	city_based_in integer REFERENCES city (id),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airport table
CREATE TABLE airport (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	icao character varying (4) NOT NULL,
	iata character varying (3) NOT NULL,
	city_id integer REFERENCES city (id),
	country_id integer REFERENCES country (id),
	number_of_runways integer NOT NULL,
	number_of_terminals integer NOT NULL,
	capacity integer,
	international boolean NOT NULL,
	junction boolean NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet table
CREATE TABLE fleet (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	iso2 character varying (2) NOT NULL,
	iso3 character varying (3) NOT NULL,
	call_sign character varying,
	hub_airport_id integer REFERENCES airport (id),
	country_id integer REFERENCES country (id),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create airplane table
CREATE TABLE airplane (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	manufacturer_id integer REFERENCES manufacturer (id),
	capacity integer NOT NULL,
	max_range_in_km integer NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet-airplane table
CREATE TABLE fleet_airplane (
	id serial NOT NULL,
	PRIMARY KEY (id),
	fleet_id integer REFERENCES fleet (id),
	airplane_id integer REFERENCES airplane (id),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet-route table
CREATE TABLE fleet_route (
	id serial NOT NULL,
	PRIMARY KEY (id),
	fleet_airplane_id integer REFERENCES fleet_airplane (id) NOT NULL,
	route_number character varying (10) NOT NULL,
	start_airport_id integer REFERENCES airport (id) NOT NULL,
	destination_airport_id integer REFERENCES airport (id) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);