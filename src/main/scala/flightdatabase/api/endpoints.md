List of endpoints
-----------------

- Base URL: `http://localhost:18181/v1/flightdb`

### Hello-World
1. **GET /hello/{name}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/hello/me
   ```

### Airline-Airplanes
1. **HEAD /airline-airplanes/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airline-airplanes/1
   ```

2. **GET /airline-airplanes**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-airplanes
   ```

3. **GET /airline-airplanes/{id}**
   ```bash
   curl -i GET http://localhost:18181/v1/flightdb/airline-airplanes/1
   ```

4. **GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}**
   ```bash
   curl -i GET http://localhost:18181/v1/flightdb/airline-airplanes/airline/1/airplane/1
   ```

5. **GET /airline-airplanes/airline/{value}?field={airline_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-airplanes/airline/Lufthansa?field=name
   ```

6. **GET /airline-airplanes/airplane/{value}?field={airplane_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-airplanes/airplane/A380?field=name
   ```

7. **POST /airline-airplanes**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airline-airplanes -H "Content-Type: application/json" -d '{"airline_id": 1, "airplane_id": 1}'
   ```

8. **PUT /airline-airplanes/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airline-airplanes/1 -H "Content-Type: application/json" -d '{"id": 1, "airline_id": 1, "airplane_id": 2}'
   ```

9. **PATCH /airline-airplanes/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airline-airplanes/1 -H "Content-Type: application/json" -d '{"airplane_id": 3}'
   ```

10. **DELETE /airline-airplanes/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airline-airplanes/1
   ```

### Airline-Cities
1. **HEAD /airline-cities/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airline-cities/1
   ```

2. **GET /airline-cities**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-cities
   ```

3. **GET /airline-cities/{id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-cities/1
   ```

4. **GET /airline-cities/airline/{airline_id}/city/{city_id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-cities/airline/1/city/1
   ```

5. **GET /airline-cities/airline/{value}?field={airline_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-cities/airline/Lufthansa?field=name
   ```

6. **GET /airline-cities/city/{value}?field={city_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-cities/city/Bangalore?field=name
   ```

7. **POST /airline-cities**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airline-cities -H "Content-Type: application/json" -d '{"airline_id": 1, "city_id": 1}'
   ```

8. **PUT /airline-cities/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airline-cities/1 -H "Content-Type: application/json" -d '{"airline_id": 1, "city_id": 2}'
   ```

9. **PATCH /airline-cities/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airline-cities/1 -H "Content-Type: application/json" -d '{"city_id": 3}'
   ```

10. **DELETE /airline-cities/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airline-cities/1
   ```

### Airlines
1. **HEAD /airlines/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airlines/1
   ```

2. **GET /airlines?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airlines?only-names
   ```

3. **GET /airlines/{value}?field={field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airlines/Lufthansa?field=name
   ```

4. **GET /airlines/country/{value}?field={field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airlines/country/DE?field=iso2
   ```

5. **POST /airlines**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airlines -H "Content-Type: application/json" -d '{"name": "New Airline", "iata": "NA", "icao": "NAA", "call_sign": "NEWAIR", "country_id": 1}'
   ```

6. **PUT /airlines/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airlines/3 -H "Content-Type: application/json" -d '{"id": 3, "name": "Updated Airline", "iata": "UA", "icao": "UAA", "call_sign": "UPDAIR", "country_id": 1}'
   ```

7. **PATCH /airlines/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airlines/3 -H "Content-Type: application/json" -d '{"name": "Updated Airline Name"}'
   ```

8. **DELETE /airlines/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airlines/3
   ```

### Airline-Routes
1. **HEAD /airline-routes/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airline-routes/1
   ```

2. **GET /airline-routes?only-routes**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-routes?only-routes
   ```

3. **GET /airline-routes/{value}?field={airline-route-field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-routes/LH754?field=route_number
   ```

4. **GET /airline-routes/airline/{value}?field={airline_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-routes/airline/Lufthansa?field=name
   ```

5. **GET /airline-routes/airplane/{value}?field={airplane_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-routes/airplane/A380?field=name
   ```

6. **GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airline-routes/airport/FRA?field=iata&inbound&outbound
   ```

7. **POST /airline-routes**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airline-routes -H "Content-Type: application/json" -d '{"airline_airplane_id": 1, "route_number": "LH754", "start_airport_id": 1, "destination_airport_id": 2}'
   ```

8. **PUT /airline-routes/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airline-routes/1 -H "Content-Type: application/json" -d '{"airline_airplane_id": 1, "route_number": "LH755", "start_airport_id": 2, "destination_airport_id": 1}'
   ```

9. **PATCH /airline-routes/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airline-routes/1 -H "Content-Type: application/json" -d '{"route_number": "LH756"}'
   ```

10. **DELETE /airline-routes/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airline-routes/1
   ```

### Airplanes
1. **HEAD /airplanes/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airplanes/1
   ```

2. **GET /airplanes?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airplanes?only-names
   ```

3. **GET /airplanes/{value}?field={airplane_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airplanes/A380?field=name
   ```

4. **GET /airplanes/manufacturer/{value}?field={manufacturer_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airplanes/manufacturer/Airbus?field=name
   ```

5. **POST /airplanes**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airplanes -H "Content-Type: application/json" -d '{"name": "A350", "manufacturer_id": 1, "capacity": 440, "max_range_in_km": 15000}'
   ```

6. **PUT /airplanes/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airplanes/1 -H "Content-Type: application/json" -d '{"name": "A380-800", "manufacturer_id": 1, "capacity": 853, "max_range_in_km": 14800}'
   ```

7. **PATCH /airplanes/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airplanes/1 -H "Content-Type: application/json" -d '{"name": "A380-800 Plus"}'
   ```

8. **DELETE /airplanes/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airplanes/1
   ```

### Airports
1. **HEAD /airports/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/airports/1
   ```

2. **GET /airports?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airports?only-names
   ```

3. **GET /airports/{value}?field={airport_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airports/FRA?field=iata
   ```

4. **GET /airports/city/{value}?field={city_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airports/city/Bangalore?field=name
   ```

5. **GET /airports/country/{value}?field={country_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/airports/country/IN?field=iso2
   ```

6. **POST /airports**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/airports -H "Content-Type: application/json" -d '{"name": "New Airport", "icao": "ICAO", "iata": "IATA", "city_id": 1, "number_of_runways": 2, "number_of_terminals": 1, "capacity": 100000, "international": true, "junction": false}'
   ```

7. **PUT /airports/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/airports/1 -H "Content-Type: application/json" -d '{"name": "Updated Airport", "icao": "ICAO", "iata": "IATA", "city_id": 1, "number_of_runways": 2, "number_of_terminals": 1, "capacity": 100000, "international": true, "junction": false}'
   ```

8. **PATCH /airports/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/airports/1 -H "Content-Type: application/json" -d '{"name": "Updated Airport Name"}'
   ```

9. **DELETE /airports/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/airports/1
   ```

### Cities
1. **HEAD /cities/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/cities/1
   ```

2. **GET /cities?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/cities?only-names
   ```

3. **GET /cities/{value}?field={city_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/cities/Bangalore?field=name
   ```

4. **GET /cities/country/{value}?field={country_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/cities/country/IN?field=iso2
   ```

5. **POST /cities**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/cities -H "Content-Type: application/json" -d '{"name": "New City", "country_id": 1, "capital": false, "population": 100000, "latitude": 12.9715987, "longitude": 77.5945627, "timezone": "Asia/Kolkata"}'
   ```

6. **PUT /cities/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/cities/1 -H "Content-Type: application/json" -d '{"name": "Updated City", "country_id": 1, "capital": false, "population": 100000, "latitude": 12.9715987, "longitude": 77.5945627, "timezone": "Asia/Kolkata"}'
   ```

7. **PATCH /cities/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/cities/1 -H "Content-Type: application/json" -d '{"name": "Updated City Name"}'
   ```

8. **DELETE /cities/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/cities/1
   ```

### Countries
1. **HEAD /countries/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/countries/1
   ```

2. **GET /countries?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/countries?only-names
   ```

3. **GET /countries/{value}?field={country_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/countries/IN?field=iso2
   ```

4. **GET /countries/language/{value}?field={language_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/countries/language/EN?field=iso2
   ```

5. **GET /countries/currency/{value}?field={currency_field; default: id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/countries/currency/USD?field=iso
   ```

6. **POST /countries**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/countries -H "Content-Type: application/json" -d '{"name": "India", "iso2": "IN", "iso3": "IND", "country_code": 91, "domain_name": ".in", "main_language_id": 1, "secondary_language_id": 2, "tertiary_language_id": 3, "currency_id": 1, "nationality": "Indian"}'
   ```

7. **PUT /countries/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/countries/1 -H "Content-Type: application/json" -d '{"name": "India", "iso2": "IN", "iso3": "IND", "country_code": 91, "domain_name": ".in", "main_language_id": 1, "secondary_language_id": 2, "tertiary_language_id": 3, "currency_id": 1, "nationality": "Indian"}'
   ```

8. **PATCH /countries/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/countries/1 -H "Content-Type: application/json" -d '{"name": "Republic of India"}'
   ```

9. **DELETE /countries/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/countries/1
   ```

### Currencies
1. **HEAD /currencies/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/currencies/1
   ```

2. **GET /currencies?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/currencies?only-names
   ```

3. **GET /currencies/{value}?field={currency_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/currencies/USD?field=iso
   ```

4. **POST /currencies**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/currencies -H "Content-Type: application/json" -d '{"name": "Japanese Yen", "iso": "JPY", "symbol": "¥"}'
   ```

5. **PUT /currencies/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/currencies/1 -H "Content-Type: application/json" -d '{"name": "US Dollar", "iso": "USD", "symbol": "$"}'
   ```

6. **PATCH /currencies/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/currencies/1 -H "Content-Type: application/json" -d '{"name": "US Dollar"}'
   ```

7. **DELETE /currencies/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/currencies/1
   ```

### Languages
1. **HEAD /languages/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/languages/1
   ```

2. **GET /languages?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/languages?only-names
   ```

3. **GET /languages/{value}?field={language_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/languages/EN?field=iso2
   ```

4. **POST /languages**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/languages -H "Content-Type: application/json" -d '{"name": "Spanish", "iso2": "ES", "iso3": "ESP", "original_name": "Español"}'
   ```

5. **PUT /languages/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/languages/1 -H "Content-Type: application/json" -d '{"name": "English", "iso2": "EN", "iso3": "ENG", "original_name": "English"}'
   ```

6. **PATCH /languages/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/languages/1 -H "Content-Type: application/json" -d '{"name": "British English"}'
   ```

7. **DELETE /languages/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/languages/1
   ```

### Manufacturers
1. **HEAD /manufacturers/{id}**
   ```bash
   curl -I -X HEAD http://localhost:18181/v1/flightdb/manufacturers/1
   ```

2. **GET /manufacturers?only-names**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/manufacturers?only-names
   ```

3. **GET /manufacturers/{value}?field={manufacturer_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/manufacturers/Airbus?field=name
   ```

4. **GET /manufacturers/city/{value}?field={city_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/manufacturers/city/Leiden?field=name
   ```

5. **GET /manufacturers/country/{value}?field={country_field; default=id}**
   ```bash
   curl -i http://localhost:18181/v1/flightdb/manufacturers/country/Netherlands?field=name
   ```

6. **POST /manufacturers**
   ```bash
   curl -i -X POST http://localhost:18181/v1/flightdb/manufacturers -H "Content-Type: application/json" -d '{"name": "Airbus", "base_city_id": 1}'
   ```

7. **PUT /manufacturers/{id}**
   ```bash
   curl -i -X PUT http://localhost:18181/v1/flightdb/manufacturers/1 -H "Content-Type: application/json" -d '{"name": "Boeing", "base_city_id": 2}'
   ```

8. **PATCH /manufacturers/{id}**
   ```bash
   curl -i -X PATCH http://localhost:18181/v1/flightdb/manufacturers/1 -H "Content-Type: application/json" -d '{"name": "Airbus"}'
   ```

9. **DELETE /manufacturers/{id}**
   ```bash
   curl -i -X DELETE http://localhost:18181/v1/flightdb/manufacturers/1
   ```