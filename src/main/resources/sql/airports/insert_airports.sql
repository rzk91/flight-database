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
 
INSERT INTO airport 
       (name, icao, iata, city_id, country_id, hub_to, 
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Kempegowda International Airport', 'VOBL', 'BLR',
       SELECT id FROM city WHERE name = 'Bangalore',
       SELECT id FROM country WHERE iso2 = 'IN',
       null,
       2, 2, 16800000,
       true, false
   );
 
INSERT INTO airport 
       (name, icao, iata, city_id, country_id, hub_to, 
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Dubai International Airport', 'OMDB', 'DXB',
       SELECT id FROM city WHERE name = 'Dubai',
       SELECT id FROM country WHERE iso2 = 'AE',
       SELECT id FROM fleet WHERE name = 'Emirates',
       2, 3, 92500000,
       true, false
   );
 
