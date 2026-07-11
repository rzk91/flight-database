ALTER TABLE airport ADD COLUMN latitude numeric;
ALTER TABLE airport ADD COLUMN longitude numeric;
ALTER TABLE airport ADD COLUMN taxi_out_duration integer;
ALTER TABLE airport ADD COLUMN taxi_in_duration integer;

COMMENT ON COLUMN airport.taxi_out_duration IS 'Typical taxi-out duration, in minutes.';
COMMENT ON COLUMN airport.taxi_in_duration IS 'Typical taxi-in duration, in minutes.';

UPDATE airport SET latitude = 50.0333, longitude = 8.5706, taxi_out_duration = 18, taxi_in_duration = 8
    WHERE icao = 'EDDF';
UPDATE airport SET latitude = 13.1986, longitude = 77.7066, taxi_out_duration = 12, taxi_in_duration = 6
    WHERE icao = 'VOBL';
UPDATE airport SET latitude = 25.2532, longitude = 55.3657, taxi_out_duration = 15, taxi_in_duration = 7
    WHERE icao = 'OMDB';

ALTER TABLE airport ALTER COLUMN latitude SET NOT NULL;
ALTER TABLE airport ALTER COLUMN longitude SET NOT NULL;
ALTER TABLE airport ALTER COLUMN taxi_out_duration SET NOT NULL;
ALTER TABLE airport ALTER COLUMN taxi_in_duration SET NOT NULL;

ALTER TABLE airport ADD CONSTRAINT airport_taxi_out_duration_check CHECK (taxi_out_duration > 0);
ALTER TABLE airport ADD CONSTRAINT airport_taxi_in_duration_check CHECK (taxi_in_duration > 0);
