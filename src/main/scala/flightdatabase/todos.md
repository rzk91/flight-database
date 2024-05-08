TODO list
----------

- ~~Perhaps consider using `fly4s` instead of the original Java implementation of `Flyway`.~~ Not needed for now.
- ~~Rename `services` to `endpoints` and accordingly all file names, e.g., `AirplaneService` to `AirplaneEndpoint`.~~
  DONE!
- ~~Refactor the project structure to introduce a domain package that will contain separate sub-packages for each
  database object. Each of these sub-packages will include two files:~~ DONE!
    - ~~A {dbObject}Model.scala file that defines the case class for the database object.~~ DONE!
    - ~~A {dbObject}Algebra.scala file that defines a trait encapsulating all possible queries that can be run against
      the corresponding table. This serves as an abstraction layer between the endpoints and the actual queries.~~ DONE!
- ~~For instance, for the `language` table, there would be a `language` package under domain
  with `LanguageModel.scala` (defining the `LanguageModel` case class) and `LanguageAlgebra`.scala (defining
  the `LanguageAlgebra` trait with methods like `get`, `getById`, etc.). This structure allows for clear separation of
  concerns and makes it easy to switch out the database library (e.g., doobie) used for the actual implementation of the
  queries.~~ DONE!
- ~~Compose basic algebraic operations in all `{dbObject}Algebra` traits,
  e.g., `get`, `getById`, `insert`, `update`, `delete`, etc.~~ DONE!
- ~~Rewrite the `endpoints` to call the right algebra based on the routes and then convert the query output to
  a `Response`.~~ DONE!
- ~~In the package `repository`, we will have the implementation of the services that we offer with respect to the
  database library. Perhaps a subpackage called `doobie` for all doobie-based queries. This would also perform query
  execution in the same code.~~ DONE!
- ~~Fix all domain models after changes to ModelBase.~~ DONE!
- ~~Upgrade all dependencies, especially doobie and http4s since they have some major changes.~~ DONE!
- ~~Add logging for doobie queries.~~ DONE!
- ~~Write tests for all generic doobie queries.~~ DONE!
- ~~Implement all doobie queries.~~ DONE for now... more will be added as needed.
- ~~Write tests for all specific doobie queries.~~ DONE!
- ~~Implement all endpoints using algebras.~~ DONE!
- ~~Rename `Fleet` to `Airline` everywhere!~~ DONE!
- ~~Introduce `AirlineCity` as a junction table to account for multiple hubs per airline.~~ DONE!
- ~~Write integration tests for all algebras.~~ DONE!
- ~~Make all endpoints uniform.~~ DONE!
- Comparison queries for all endpoints. (i.e., `GET /cities/filter?field=population&operator=gt&value=1000000` should
  return all cities with
  a population greater than 1 million. Convention slightly based
  on https://developer.adobe.com/commerce/webapi/rest/use-rest/performing-searches/)
- Introduce fuzzy search? Or at least case-insensitive search?
- Make it possible to select a set of fields and not only one or all.
- All `endpoints` should return URI of the output resource in the `Location` header. Basically, the `id` field should be
  a JSON object with a `uri` field. This will allow for easy navigation of the API.
- Add a URI to all foreign key fields in the response (maybe use something similar to `TableBase`).
- Write unit/integration/both tests for all endpoints.
- Expand API endpoint list to include more complex operations.
- Move domain to a separate module and rename `src` to `core`.
- Update the `README.md` to reflect the changes in the project structure and the new features.
- Upgrade to sbt 1.9.9 and restructure build.sbt to account for multiple modules.
- Add a `Dockerfile` and `docker-compose.yml` to run the application in a container.
- Start working on the frontend using ScalaJS!
