ALTER TABLE airport ADD COLUMN latitude numeric;
ALTER TABLE airport ADD COLUMN longitude numeric;
ALTER TABLE airport ADD COLUMN typical_taxi_out_minutes integer;
ALTER TABLE airport ADD COLUMN typical_taxi_in_minutes integer;

UPDATE airport SET latitude = 50.0333, longitude = 8.5706, typical_taxi_out_minutes = 18, typical_taxi_in_minutes = 8
    WHERE icao = 'EDDF';
UPDATE airport SET latitude = 13.1986, longitude = 77.7066, typical_taxi_out_minutes = 12, typical_taxi_in_minutes = 6
    WHERE icao = 'VOBL';
UPDATE airport SET latitude = 25.2532, longitude = 55.3657, typical_taxi_out_minutes = 15, typical_taxi_in_minutes = 7
    WHERE icao = 'OMDB';

ALTER TABLE airport ALTER COLUMN latitude SET NOT NULL;
ALTER TABLE airport ALTER COLUMN longitude SET NOT NULL;
ALTER TABLE airport ALTER COLUMN typical_taxi_out_minutes SET NOT NULL;
ALTER TABLE airport ALTER COLUMN typical_taxi_in_minutes SET NOT NULL;

ALTER TABLE airport ADD CONSTRAINT airport_typical_taxi_out_minutes_check CHECK (typical_taxi_out_minutes > 0);
ALTER TABLE airport ADD CONSTRAINT airport_typical_taxi_in_minutes_check CHECK (typical_taxi_in_minutes > 0);
