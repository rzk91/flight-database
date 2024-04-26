INSERT INTO airline_airplane
  	(airline_id, airplane_id)
	VALUES (
  	(SELECT id FROM airline WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = '747-8')
	);

INSERT INTO airline_airplane
  	(airline_id, airplane_id)
	VALUES (
  	(SELECT id FROM airline WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = 'A380')
	);

INSERT INTO airline_airplane
  	(airline_id, airplane_id)
	VALUES (
  	(SELECT id FROM airline WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = 'A320neo')
	);

INSERT INTO airline_airplane
  	(airline_id, airplane_id)
	VALUES (
  	(SELECT id FROM airline WHERE name = 'Emirates'),
  	(SELECT id FROM airplane WHERE name = 'A380')
	);

INSERT INTO airline_airplane
  	(airline_id, airplane_id)
	VALUES (
  	(SELECT id FROM airline WHERE name = 'Emirates'),
  	(SELECT id FROM airplane WHERE name = 'A320neo')
	);

ALTER SEQUENCE airline_airplane_id_seq RESTART WITH 6;