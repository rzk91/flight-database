INSERT INTO manufacturer (name, base_city_id)
 VALUES ('Airbus', (SELECT id FROM city WHERE name = 'Leiden'));
        
INSERT INTO manufacturer (name, base_city_id)
 VALUES ('Boeing', (SELECT id FROM city WHERE name = 'Chicago'));
        
ALTER SEQUENCE manufacturer_id_seq RESTART WITH 3;