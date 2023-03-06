-- Unique indices on language
ALTER TABLE language ADD UNIQUE (iso2);
ALTER TABLE language ADD UNIQUE (iso3);

-- Unique indices on currency
ALTER TABLE currency ADD UNIQUE (iso);
ALTER TABLE currency ADD UNIQUE NULLS NOT DISTINCT (symbol);

-- Unique indices on country
ALTER TABLE country ADD UNIQUE (iso2);
ALTER TABLE country ADD UNIQUE (iso3);
ALTER TABLE country ADD UNIQUE (country_code);
ALTER TABLE country ADD UNIQUE NULLS NOT DISTINCT (domain_name);
ALTER TABLE country ADD UNIQUE (nationality);

-- Unique indices on city
ALTER TABLE city ADD UNIQUE (name, country_id);

-- Unique indices on manufacturer
ALTER TABLE manufacturer ADD UNIQUE (name);

-- Unique indices on airport
ALTER TABLE airport ADD UNIQUE (iata);
ALTER TABLE airport ADD UNIQUE (icao);
ALTER TABLE airport ADD UNIQUE (name, city_id);

-- Unique indices on fleet
ALTER TABLE fleet ADD UNIQUE (iso2);
ALTER TABLE fleet ADD UNIQUE (iso3);
ALTER TABLE fleet ADD UNIQUE (name, hub_airport_id);
ALTER TABLE fleet ADD UNIQUE NULLS NOT DISTINCT (call_sign);

-- Unique indices on airplane
ALTER TABLE airplane ADD UNIQUE (name);

-- Unique indices on fleet_airplane
ALTER TABLE fleet_airplane ADD UNIQUE (fleet_id, airplane_id);

-- Unique indices on fleet_route
ALTER TABLE fleet_route ADD UNIQUE (route_number, start_airport_id, destination_airport_id);