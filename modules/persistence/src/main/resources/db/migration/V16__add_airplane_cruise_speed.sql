ALTER TABLE airplane ADD COLUMN cruise_speed integer;

COMMENT ON COLUMN airplane.cruise_speed IS 'Typical cruise speed, in km/h.';

UPDATE airplane SET cruise_speed = 903 WHERE name = 'A380';
UPDATE airplane SET cruise_speed = 907 WHERE name = '747-8';
UPDATE airplane SET cruise_speed = 828 WHERE name = 'A320neo';
UPDATE airplane SET cruise_speed = 903 WHERE name = '787-8';

ALTER TABLE airplane ALTER COLUMN cruise_speed SET NOT NULL;

ALTER TABLE airplane ADD CONSTRAINT airplane_cruise_speed_check CHECK (cruise_speed > 0);
