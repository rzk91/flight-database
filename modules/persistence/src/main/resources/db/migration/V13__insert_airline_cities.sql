INSERT INTO airline_city
    (airline_id, city_id)
    VALUES (
    (SELECT id FROM airline WHERE name = 'Lufthansa'),
    (SELECT id FROM city WHERE name = 'Frankfurt am Main')
   );

INSERT INTO airline_city
    (airline_id, city_id)
    VALUES (
    (SELECT id FROM airline WHERE name = 'Emirates'),
    (SELECT id FROM city WHERE name = 'Dubai')
   );

ALTER SEQUENCE airline_city_id_seq RESTART WITH 3;