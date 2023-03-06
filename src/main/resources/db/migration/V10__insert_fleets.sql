INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_airport_id)
   VALUES (
       'Lufthansa', 'LH', 'DLH', 'Lufthansa',
       (SELECT id FROM airport WHERE iata = 'FRA')
   );
 
INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_airport_id)
   VALUES (
       'Emirates', 'EK', 'UAE', 'Emirates',
       (SELECT id FROM airport WHERE iata = 'DXB')
   );
 
ALTER SEQUENCE fleet_id_seq RESTART WITH 3;