INSERT INTO currency (name, iso, symbol) VALUES ('Indian Rupee', 'INR', '₹');
INSERT INTO currency (name, iso, symbol) VALUES ('Euro', 'EUR', '€');
INSERT INTO currency (name, iso, symbol) VALUES ('Swedish Krona', 'SEK', 'kr');
INSERT INTO currency (name, iso, symbol) VALUES ('Dirham', 'AED', null);
INSERT INTO currency (name, iso, symbol) VALUES ('US Dollar', 'USD', '$');

ALTER SEQUENCE currency_id_seq RESTART WITH 6;
