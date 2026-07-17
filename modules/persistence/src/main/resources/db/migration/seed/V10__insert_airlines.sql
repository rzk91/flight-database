INSERT INTO airline
       (name, iata, icao, call_sign, country_id)
   VALUES (
       'Lufthansa', 'LH', 'DLH', 'LUFTHANSA',
       (SELECT id FROM country WHERE name = 'Germany')
   );
 
INSERT INTO airline
       (name, iata, icao, call_sign, country_id)
   VALUES (
       'Emirates', 'EK', 'UAE', 'EMIRATES',
       (SELECT id FROM country WHERE name = 'United Arab Emirates')
   );
 
ALTER SEQUENCE airline_id_seq RESTART WITH 3;