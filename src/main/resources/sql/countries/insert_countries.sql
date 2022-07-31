INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'India', 'IN', 'IND', 91,
       '.co.in',
       SELECT id FROM language WHERE name = 'Hindi',
       SELECT id FROM language WHERE name = 'English',
       SELECT id FROM language WHERE name = 'Tamil',
       SELECT id FROM currency WHERE name = 'Indian Rupee',
       'Indian'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'Germany', 'DE', 'DEU', 49,
       '.de',
       SELECT id FROM language WHERE name = 'German',
       null,
       null,
       SELECT id FROM currency WHERE name = 'Euro',
       'German'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'Sweden', 'SE', 'SWE', 46,
       '.se',
       SELECT id FROM language WHERE name = 'Swedish',
       null,
       null,
       SELECT id FROM currency WHERE name = 'Swedish Krona',
       'Swede'
   );
 
