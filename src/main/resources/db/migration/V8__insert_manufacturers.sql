INSERT INTO manufacturer (name, city_based_in)
 VALUES ('Airbus', (SELECT id FROM city WHERE name = 'Leiden'));
        
INSERT INTO manufacturer (name, city_based_in)
 VALUES ('Boeing', (SELECT id FROM city WHERE name = 'Chicago'));
        
ALTER SEQUENCE manufacturer_id_seq RESTART WITH 3;