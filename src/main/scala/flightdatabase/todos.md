TODO list
----------

- ~~Rename `services` to `endpoints` and accordingly all file names, e.g., `AirplaneService` to `AirplaneEndpoint`.~~ DONE!
- ~~Refactor the project structure to introduce a domain package that will contain separate sub-packages for each database object. Each of these sub-packages will include two files:~~ DONE!
	- ~~A {dbObject}Model.scala file that defines the case class for the database object.~~ DONE!
	- ~~A {dbObject}Algebra.scala file that defines a trait encapsulating all possible queries that can be run against the corresponding table. This serves as an abstraction layer between the endpoints and the actual queries.~~ DONE!
- ~~For instance, for the `language` table, there would be a `language` package under domain with `LanguageModel.scala` (defining the `LanguageModel` case class) and `LanguageAlgebra`.scala (defining the `LanguageAlgebra` trait with methods like `get`, `getById`, etc.). This structure allows for clear separation of concerns and makes it easy to switch out the database library (e.g., doobie) used for the actual implementation of the queries.~~ DONE!
- Compose basic algebraic operations in all `{dbObject}Algebra` traits, e.g., `get`, `getById`, `insert`, `update`, `delete`, etc.
- Rewrite the `endpoints` to call the right algebra based on the routes and then convert the query output to a `Response`.
- In the package `repository`, we will have the implementation of the services that we offer with respect to the database library. Perhaps a subpackage called `doobie` for all doobie-based queries. This would also perform query execution in the same code.
- Upgrade all dependencies, especially doobie and http4s since they have some major changes
- Perhaps consider using `fly4s` instead of the original Java implementation of `Flyway`.
