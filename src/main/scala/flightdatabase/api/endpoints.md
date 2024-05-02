List of endpoints
-----------------

- Base URL: `http://localhost:18181/v1/flightdb`

### Hello-World
- GET /hello/{name}

### Airline-Airplanes
- HEAD /airline-airplanes/{id}
- GET /airline-airplanes
- GET /airline-airplanes/{id}
- GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
- GET /airline-airplanes/airline/{value}?field={airline_field; default: id}
- GET /airline-airplanes/airplane/{value}?field={airplane_field; default: id}
- POST /airline-airplanes
- PUT /airline-airplanes/{id}
- PATCH /airline-airplanes/{id}
- DELETE /airline-airplanes/{id}

### Airline-Cities
- HEAD /airline-cities/{id}
- GET /airline-cities
- GET /airline-cities/{id}
- GET /airline-cities/airline/{airline_id}/city/{city_id}
- GET /airline-cities/airline/{value}?field={airline_field; default: id}
- GET /airline-cities/city/{value}?field={city_field; default: id}
- POST /airline-cities
- PUT /airline-cities/{id}
- PATCH /airline-cities/{id}
- DELETE /airline-cities/{id}

### Airlines
- HEAD /airlines/{id}
- GET /airlines?only-names
- GET /airlines/{value}?field={field; default: id}
- GET /airlines/country/{value}?field={field; default: id}
- POST /airlines
- PUT /airlines/{id}
- PATCH /airlines/{id}
- DELETE /airlines/{id}

### Airline-Routes
- HEAD /airline-routes/{id}
- GET /airline-routes?only-routes
- GET /airline-routes/{value}?field={airline-route-field; default: id}
- GET /airline-routes/airline/{value}?field={airline_field; default: id}
- GET /airline-routes/airplane/{value}?field={airplane_field; default: id}
- GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound
- POST /airline-routes
- PUT /airline-routes/{id}
- PATCH /airline-routes/{id}
- DELETE /airline-routes/{id}

### Airplanes
- HEAD /airplanes/{id}
- GET /airplanes?only-names
- GET /airplanes/{value}?field={airplane_field; default: id}
- GET /airplanes/manufacturer/{value}?field={manufacturer_field; default: id}
- POST /airplanes
- PUT /airplanes/{id}
- PATCH /airplanes/{id}
- DELETE /airplanes/{id}

### Airports
- HEAD /airports/{id}
- GET /airports?only-names
- GET /airports/{value}?field={airport_field; default: id}
- GET /airports/city/{value}?field={city_field; default: id}
- GET /airports/country/{value}?field={country_field; default: id}
- POST /airports
- PUT /airports/{id}
- PATCH /airports/{id}
- DELETE /airports/{id}

### Cities
- HEAD /cities/{id}
- GET /cities?only-names
- GET /cities/{value}?field={city_field; default=id}
- GET /cities/country/{value}?field={country_field; default=id}
- POST /cities
- PUT /cities/{id}
- PATCH /cities/{id}
- DELETE /cities/{id}

### Countries
- HEAD /countries/{id}
- GET /countries?onlyNames
- GET /countries/{value}?field={country_field; default=id}
- GET /countries/language/{value}?field={language_field; default: id}
- GET /countries/currency/{value}?field={currency_field; default: id}
- POST /countries
- PUT /countries/{id}
- PATCH /countries/{id}
- DELETE /countries/{id}

### Currencies
- HEAD /currencies/{id}
- GET /currencies?only-names
- GET /currencies/{value}?field={currency_field; default=id}
- POST /currencies
- PUT /currencies/{id}
- PATCH /currencies/{id}
- DELETE /currencies/{id}

### Languages
- HEAD /languages/{id}
- GET /languages?only-names
- GET /languages/{value}?field={language_field; default=id}
- POST /languages
- PUT /languages/{id}
- PATCH /languages/{id}
- DELETE /languages/{id}

##### Possible additions
- GET /languages/search?q={query}
- e.g., GET /languages/search?q={id=1}
- e.g., GET /languages/search?q={name=English}
- e.g., GET /languages/search?q={name=English;OR;name=Tamil} ???
- e.g., GET /languages/search?q={name=English OR Tamil} ???

### Manufacturers
- HEAD /manufacturers/{id}
- GET /manufacturers?only-names
- GET /manufacturers/{value}?field={manufacturer_field; default=id}
- GET /manufacturers/city/{value}?field={city_field; default=id}
- GET /manufacturers/country/{value}?field={country_field; default=id}
- POST /manufacturers
- PUT /manufacturers/{id}
- PATCH /manufacturers/{id}
- DELETE /manufacturers/{id}