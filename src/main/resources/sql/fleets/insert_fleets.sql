INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_at, country_id)
   VALUES (
       'Lufthansa', 'LH', 'DLH', 'Lufthansa',
       (SELECT id FROM city WHERE name = 'Frankfurt am Main'),
       (SELECT id FROM country WHERE iso2 = 'DE')
   );
 
INSERT INTO fleet 
       (name, iso2, iso3, call_sign, hub_at, country_id)
   VALUES (
       'Emirates', 'EK', 'UAE', 'Emirates',
       (SELECT id FROM city WHERE name = 'Dubai'),
       (SELECT id FROM country WHERE iso2 = 'AE')
   );
 
