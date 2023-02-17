INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_at, country_id)
   VALUES (
       'Lufthansa', 'LH', 'DLH', 'Lufthansa',
       (SELECT id FROM airport WHERE iata = 'FRA'),
       (SELECT id FROM country WHERE iso2 = 'DE')
   );
 
INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_at, country_id)
   VALUES (
       'Emirates', 'EK', 'UAE', 'Emirates',
       (SELECT id FROM airport WHERE iata = 'DXB'),
       (SELECT id FROM country WHERE iso2 = 'AE')
   );
 
