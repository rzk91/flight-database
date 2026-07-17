INSERT INTO airport
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity,
       international, junction,
       latitude, longitude, taxi_out_duration, taxi_in_duration)
   VALUES (
       'Frankfurt am Main Airport', 'EDDF', 'FRA',
       (SELECT id FROM city WHERE name = 'Frankfurt am Main'),
       4, 3, 65000000,
       true, true,
       50.0333, 8.5706, 18, 8
   );

INSERT INTO airport
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity,
       international, junction,
       latitude, longitude, taxi_out_duration, taxi_in_duration)
   VALUES (
       'Kempegowda International Airport', 'VOBL', 'BLR',
       (SELECT id FROM city WHERE name = 'Bangalore'),
       2, 2, 16800000,
       true, false,
       13.1986, 77.7066, 12, 6
   );

INSERT INTO airport
       (name, icao, iata, city_id,
       number_of_runways, number_of_terminals, capacity,
       international, junction,
       latitude, longitude, taxi_out_duration, taxi_in_duration)
   VALUES (
       'Dubai International Airport', 'OMDB', 'DXB',
       (SELECT id FROM city WHERE name = 'Dubai'),
       2, 3, 92500000,
       true, true,
       25.2532, 55.3657, 15, 7
   );

ALTER SEQUENCE airport_id_seq RESTART WITH 4;
