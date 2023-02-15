INSERT INTO manufacturer (name, based_in)
 VALUES (Airbus, SELECT id FROM city WHERE name = 'Leiden');
        
INSERT INTO manufacturer (name, based_in)
 VALUES (Boeing, SELECT id FROM city WHERE name = 'Chicago');
        
