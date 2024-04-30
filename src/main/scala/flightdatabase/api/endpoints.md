List of endpoints
-----------------

- Base URL: `http://localhost:18181/v1/flightdb`

### Airline-Airplanes
- HEAD /airline-airplanes/{id}
- GET /airline-airplanes
- GET /airline-airplanes/{id}
- GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
- GET /airline-airplanes/airplane/{value}?field={airplane_field; default: name}
- GET /airline-airplanes/airline/{value}?field={airline_field; default: name}
- POST /airline-airplanes
- PUT /airline-airplanes/{id}
- PATCH /airline-airplanes/{id}
- DELETE /airline-airplanes/{id}

### Airline-Cities
- HEAD /airline-cities/{id}
- GET /airline-cities
- GET /airline-cities/{id}
- GET /airline-cities/airline/{airline_id}/city/{city_id}
- GET /airline-cities/city/{value}?field={city_field; default: name}
- GET /airline-cities/airline/{value}?field={airline_field; default: name}
- POST /airline-cities
- PUT /airline-cities/{id}
- PATCH /airline-cities/{id}
- DELETE /airline-cities/{id}

### Airlines
- HEAD /airlines/{id}
- GET /airlines?only-names
- GET /airlines/{value}?field={field; default: id}
- GET /airlines/country/{value}?field={field; default: name}
- POST /airlines
- PUT /airlines/{id}
- PATCH /airlines/{id}
- DELETE /airlines/{id}

### Airline-Routes
- HEAD /airline-routes/{id}
- GET /airline-routes?only-routes
- GET /airline-routes/{value}?field={airline_route_field; default: id}
- GET /airline-routes/airline/{value}?field={airline_field; default: id}
- GET /airline-routes/airplane/{value}?field={airplane_field; default: id}
- GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound
- POST /airline-routes
- PUT /airline-routes/{id}
- PATCH /airline-routes/{id}
- DELETE /airline-routes/{id}