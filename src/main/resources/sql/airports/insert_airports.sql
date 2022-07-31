INSERT INTO airport 
       (name, icao, iata, city_id, country_id, hub_to, 
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Frankfurt am Main Airport', 'EDDF', 'FRA',
       SELECT id FROM city WHERE name = 'Frankfurt am Main',
       SELECT id FROM country WHERE iso2 = 'DE',
       SELECT id FROM fleet WHERE name = 'Lufthansa',
       4, 3, 65000000,
       true, true
   );