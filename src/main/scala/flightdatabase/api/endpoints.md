List of possible endpoints
---------------------------

### Language
Using the `language` table as an example for some simple set of endpoints where the queries do not depend on other tables.
1. **Basic CRUD Operations:**
   - GET /languages: Retrieve a list of all languages.
   - GET /languages/{id}: Retrieve a specific language by its ID.
   - POST /languages: Create a new language.
   - PUT /languages/{id}: Update an existing language by its ID.
   - DELETE /languages/{id}: Delete a language by its ID.

2. **Filter/Search-Based Endpoints:**
   - GET /languages?name={name}: Retrieve languages by name.
   - GET /languages?iso2={iso2}: Retrieve languages by ISO 2-letter code.
   - GET /languages?iso3={iso3}: Retrieve languages by ISO 3-letter code.
   - GET /languages?name_contains={substring}: Retrieve languages whose names contain a specific substring.
   - GET /languages?iso2_starts_with={prefix}: Retrieve languages with ISO 2-letter codes that start with a specific prefix.
   - GET /languages?iso3_ends_with={suffix}: Retrieve languages with ISO 3-letter codes that end with a specific suffix.
   - GET /languages?created_after={date}: Retrieve languages created after a certain date.
   - GET /languages?updated_before={date}: Retrieve languages that were last updated before a certain date.
   - GET /languages?sort_by={attribute}&order={asc/desc}: Retrieve languages sorted by a specific attribute (e.g., name, ISO code) in either ascending or descending order.
   - GET /languages?limit={limit}&offset={offset}: Paginate the results by specifying a limit on the number of languages returned per request and an offset to skip a certain number of results.

### Country
These endpoints are slightly complicated given the dependency to many other tables.
1. **Basic CRUD Operations:**
	- Same as the above. The complications arise when we actually implement these.
2. **Filter/Search-Based Endpoints:**
	 - Pretty much similar to the ones above but again, the complications arise due to dependency on other tables.
3. **Endpoints for Language Relationships:**
   - GET /countries/{id}/main_language: Retrieve the main language spoken in the specified country.
   - GET /countries/{id}/secondary_language: Retrieve the secondary language spoken in the specified country.
   - GET /countries/{id}/tertiary_language: Retrieve the tertiary language spoken in the specified country.
   - GET /languages/{language_id}/countries: Retrieve a list of countries where the specified language is spoken as a main, secondary, or tertiary language.

4. **Endpoints for Currency Relationship:**
   - GET /countries/{id}/currency: Retrieve the currency used in the specified country.
   - GET /currencies/{currency_id}/countries: Retrieve a list of countries that use the specified currency.

5. **Some other endpoints based on other tables:**
	- GET /cities?name={name}: Retrieve cities by name.
	- GET /cities?country_id={country_id}: Retrieve cities in a specific country.
	- GET /cities?population_greater_than={value}: Retrieve cities with a population greater than a certain value.
	- GET /cities?population_less_than={value}: Retrieve cities with a population less than a certain value.
	- GET /cities?latitude={latitude}&longitude={longitude}&radius={radius}: Retrieve cities within a certain radius of a given latitude and longitude.
Endpoints for Country Relationship:
	- GET /cities/{id}/country: Retrieve the country to which the specified city belongs.
	- GET /countries/{country_id}/cities: Retrieve a list of cities in the specified country.
