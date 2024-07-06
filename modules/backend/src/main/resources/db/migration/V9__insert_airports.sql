INSERT INTO airport 
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Frankfurt am Main Airport', 'EDDF', 'FRA',
       (SELECT id FROM city WHERE name = 'Frankfurt am Main'),
       4, 3, 65000000,
       true, true
   );
 
INSERT INTO airport 
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Kempegowda International Airport', 'VOBL', 'BLR',
       (SELECT id FROM city WHERE name = 'Bangalore'),
       2, 2, 16800000,
       true, false
   );
 
INSERT INTO airport 
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity, 
       international, junction)
   VALUES (
       'Dubai International Airport', 'OMDB', 'DXB',
       (SELECT id FROM city WHERE name = 'Dubai'),
       2, 3, 92500000,
       true, false
   );
 
ALTER SEQUENCE airport_id_seq RESTART WITH 4;