INSERT INTO airplane
       (name, manufacturer_id, capacity, max_range_in_km, cruise_speed)
   VALUES (
       'A380',
       (SELECT id FROM manufacturer WHERE name = 'Airbus'),
       853, 14800, 903
   );

INSERT INTO airplane
       (name, manufacturer_id, capacity, max_range_in_km, cruise_speed)
   VALUES (
       '747-8',
       (SELECT id FROM manufacturer WHERE name = 'Boeing'),
       410, 14310, 907
   );

INSERT INTO airplane
       (name, manufacturer_id, capacity, max_range_in_km, cruise_speed)
   VALUES (
       'A320neo',
       (SELECT id FROM manufacturer WHERE name = 'Airbus'),
       194, 6300, 828
   );

INSERT INTO airplane
       (name, manufacturer_id, capacity, max_range_in_km, cruise_speed)
   VALUES (
       '787-8',
       (SELECT id FROM manufacturer WHERE name = 'Boeing'),
       248, 13530, 903
   );

ALTER SEQUENCE airplane_id_seq RESTART WITH 5;
