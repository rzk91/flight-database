# TODO list
------------

- Rename `services` to `endpoints` and accordingly all file names, e.g., `AirplaneService` to `AirplaneEndpoint`.
- Add a new package called `Service` that includes all possible queries that one can run against a table (basically an abstraction between the endpoints and the actual queries).
	- For example, `LanguageService` would include all queries like `get`, `getById`, etc. that one could do against the `language` table.
	- This would be a bunch of `trait`s that define the services offered here irrespective of the actual implementation of them (depending on the database library used, e.g., doobie).
- In the package `repository`, we will have the implementation of the services that we offer with respect to the database library. Perhaps a subpackage called `doobie` for all doobie-based queries. This would also perform query execution in the same code.
- Rewrite the `endpoints` to call the right repository functions based on the routes and then convert the query output to a `Response`.
- Upgrade all dependencies, especially doobie and http4s since they have some major changes
- Perhaps consider using `fly4s` instead of the original Java implementation of `Flyway`.