INSERT INTO airplane 
       (name, manufacturer_id, capacity, max_range_in_km)
   VALUES (
       'A380',
       (SELECT id FROM manufacturer WHERE name = 'Airbus'),
       853, 14800
   );
 
INSERT INTO airplane 
       (name, manufacturer_id, capacity, max_range_in_km)
   VALUES (
       '747-8',
       (SELECT id FROM manufacturer WHERE name = 'Boeing'),
       410, 14310
   );
 
INSERT INTO airplane 
       (name, manufacturer_id, capacity, max_range_in_km)
   VALUES (
       'A320neo',
       (SELECT id FROM manufacturer WHERE name = 'Airbus'),
       194, 6300
   );
 
INSERT INTO airplane 
       (name, manufacturer_id, capacity, max_range_in_km)
   VALUES (
       '787-8',
       (SELECT id FROM manufacturer WHERE name = 'Boeing'),
       248, 13530
   );
 
ALTER SEQUENCE airplane_id_seq RESTART WITH 5;