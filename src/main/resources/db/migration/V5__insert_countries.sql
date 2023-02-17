INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'India', 'IN', 'IND', 91,
       '.co.in',
       (SELECT id FROM language WHERE name = 'Hindi'),
       (SELECT id FROM language WHERE name = 'English'),
       (SELECT id FROM language WHERE name = 'Tamil'),
       (SELECT id FROM currency WHERE iso = 'INR'),
       'Indian'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'Germany', 'DE', 'DEU', 49,
       '.de',
       (SELECT id FROM language WHERE name = 'German'),
       null,
       null,
       (SELECT id FROM currency WHERE iso = 'EUR'),
       'German'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'Sweden', 'SE', 'SWE', 46,
       '.se',
       (SELECT id FROM language WHERE name = 'Swedish'),
       null,
       null,
       (SELECT id FROM currency WHERE iso = 'SEK'),
       'Swede'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'United Arab Emirates', 'AE', 'ARE', 971,
       '.ae',
       (SELECT id FROM language WHERE name = 'Arabic'),
       (SELECT id FROM language WHERE name = 'English'),
       null,
       (SELECT id FROM currency WHERE iso = 'AED'),
       'Emirati'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'Netherlands', 'NL', 'NLD', 31,
       '.nl',
       (SELECT id FROM language WHERE name = 'Dutch'),
       null,
       null,
       (SELECT id FROM currency WHERE iso = 'EUR'),
       'Dutch'
   );
 
INSERT INTO country 
       (name, iso2, iso3, country_code, domain_name, 
       main_language, secondary_language, tertiary_language, 
       currency, nationality)
   VALUES (
       'United States of America', 'US', 'USA', 1,
       '.us',
       (SELECT id FROM language WHERE name = 'English'),
       (SELECT id FROM language WHERE name = 'Spanish'),
       null,
       (SELECT id FROM currency WHERE iso = 'USD'),
       'US citizen'
   );
 
