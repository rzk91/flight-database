INSERT INTO fleet_airplane
  	(fleet_id, airplane_id)
	VALUES (
  	(SELECT id FROM fleet WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = '747-8')
	);

INSERT INTO fleet_airplane
  	(fleet_id, airplane_id)
	VALUES (
  	(SELECT id FROM fleet WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = 'A380')
	);

INSERT INTO fleet_airplane
  	(fleet_id, airplane_id)
	VALUES (
  	(SELECT id FROM fleet WHERE name = 'Lufthansa'),
  	(SELECT id FROM airplane WHERE name = 'A320neo')
	);

INSERT INTO fleet_airplane
  	(fleet_id, airplane_id)
	VALUES (
  	(SELECT id FROM fleet WHERE name = 'Emirates'),
  	(SELECT id FROM airplane WHERE name = 'A380')
	);

INSERT INTO fleet_airplane
  	(fleet_id, airplane_id)
	VALUES (
  	(SELECT id FROM fleet WHERE name = 'Emirates'),
  	(SELECT id FROM airplane WHERE name = 'A320neo')
	);

ALTER SEQUENCE fleet_airplane_id_seq RESTART WITH 6;