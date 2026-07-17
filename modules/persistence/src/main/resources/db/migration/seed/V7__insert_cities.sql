INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Bangalore',
       (SELECT id FROM country WHERE iso2 = 'IN'),
       false,
       13193000, 12.978889, 77.591667,
       'Asia/Kolkata'
   );

INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Frankfurt am Main',
       (SELECT id FROM country WHERE iso2 = 'DE'),
       false,
       791000, 50.110556, 8.682222,
       'Europe/Berlin'
   );

INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Berlin',
       (SELECT id FROM country WHERE iso2 = 'DE'),
       true,
       3571000, 52.52, 13.405,
       'Europe/Berlin'
   );

INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Dubai',
       (SELECT id FROM country WHERE iso2 = 'AE'),
       false,
       3490000, 23.5, 54.5,
       'Asia/Dubai'
   );

INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Leiden',
       (SELECT id FROM country WHERE iso2 = 'NL'),
       false,
       125100, 52.16, 4.49,
       'Europe/Amsterdam'
   );

INSERT INTO city
       (name, country_id, capital, population,
       latitude, longitude, timezone)
   VALUES (
       'Chicago',
       (SELECT id FROM country WHERE iso2 = 'US'),
       false,
       8901000, 41.85003, -87.65005,
       'America/Chicago'
   );

ALTER SEQUENCE currency_id_seq RESTART WITH 7;