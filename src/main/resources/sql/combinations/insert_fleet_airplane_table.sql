CREATE TABLE fleet_airplane (
	id serial NOT NULL,
	PRIMARY KEY (id),
	fleet_id integer REFERENCES fleet (id) NOT NULL,
	airplane_id integer REFERENCES airplane (id) NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
	last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);