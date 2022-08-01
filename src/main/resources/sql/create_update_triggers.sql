-- Update triggers per table
CREATE TRIGGER update_language_last_update_time
BEFORE UPDATE ON language
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_currency_last_update_time
BEFORE UPDATE ON currency
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_country_last_update_time
BEFORE UPDATE ON country
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_city_last_update_time
BEFORE UPDATE ON city
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_fleet_last_update_time
BEFORE UPDATE ON fleet
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_airport_last_update_time
BEFORE UPDATE ON airport
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_manufacturer_last_update_time
BEFORE UPDATE ON manufacturer
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_airplane_last_update_time
BEFORE UPDATE ON airplane
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_fleet_airplane_last_update_time
BEFORE UPDATE ON fleet_airplane
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();

CREATE TRIGGER update_fleet_route_last_update_time
BEFORE UPDATE ON fleet_route
FOR EACH ROW EXECUTE PROCEDURE update_last_updated_column();