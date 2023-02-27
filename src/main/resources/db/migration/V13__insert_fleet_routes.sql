INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Lufthansa')
      AND airplane_id = (SELECT id FROM airplane WHERE name = '747-8')),
      'LH754',
      (SELECT id FROM airport WHERE iata = 'FRA'),
      (SELECT id FROM airport WHERE iata = 'BLR')
  );

INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Lufthansa')
      AND airplane_id = (SELECT id FROM airplane WHERE name = '747-8')),
      'LH755',
      (SELECT id FROM airport WHERE iata = 'BLR'),
      (SELECT id FROM airport WHERE iata = 'FRA')
  );

INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Emirates')
      AND airplane_id = (SELECT id FROM airplane WHERE name = 'A320neo')),
      'EK565',
      (SELECT id FROM airport WHERE iata = 'BLR'),
      (SELECT id FROM airport WHERE iata = 'DXB')
  );

INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Emirates')
      AND airplane_id = (SELECT id FROM airplane WHERE name = 'A320neo')),
      'EK566',
      (SELECT id FROM airport WHERE iata = 'DXB'),
      (SELECT id FROM airport WHERE iata = 'BLR')
  );

INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Emirates')
      AND airplane_id = (SELECT id FROM airplane WHERE name = 'A380')),
      'EK47',
      (SELECT id FROM airport WHERE iata = 'DXB'),
      (SELECT id FROM airport WHERE iata = 'FRA')
  );

INSERT INTO fleet_route
    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
  VALUES (
    (SELECT id FROM fleet_airplane 
      WHERE fleet_id = (SELECT id FROM fleet WHERE name = 'Emirates')
      AND airplane_id = (SELECT id FROM airplane WHERE name = 'A380')),
      'EK46',
      (SELECT id FROM airport WHERE iata = 'FRA'),
      (SELECT id FROM airport WHERE iata = 'DXB')
  );

