-- Create flight database
CREATE DATABASE flights;

-- Create language table
CREATE TABLE language (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	iso2 character varying (2),
	iso3 character varying (3),
	original_name character varying,
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
	main_language integer REFERENCES language (id),
	secondary_language integer REFERENCES language (id),
	tertiary_language integer REFERENCES language (id),
	currency character varying NOT NULL,
	nationality character varying NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create city table
CREATE TABLE city (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	country_id integer REFERENCES country (id),
	capital boolean NOT NULL,
	population integer NOT NULL,
	latitude numeric NOT NULL,
	longitude numeric NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create fleet table
CREATE TABLE fleet (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	call_name character varying,
	hub_at integer REFERENCES airport (id),
	country_id integer REFERENCES country (id),
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
	hub_to integer REFERENCES fleet (id),
	number_of_runways integer NOT NULL,
	number_of_terminals integer NOT NULL,
	capacity integer,
	international boolean NOT NULL,
	junction boolean NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create manufacturer table
CREATE TABLE manufacturer (
	id serial NOT NULL,
	PRIMARY KEY (id),
	name character varying NOT NULL,
	based_in integer REFERENCES city (id),
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
	max_range_km  integer NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);