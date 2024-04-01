# List of possible endpoints
-----------------------------

### Language
These endpoints serve as an example of a simple set of endpoints without any dependency to other tables.
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
2. **Filter/Search-Based Endpoints:**