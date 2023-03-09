# Flight Database
A Scala-based codebase of some microservices using [http4s](http://http4s.org/), [doobie](http://tpolecat.github.io/doobie/),
and [circe](https://github.com/circe/circe). These microservices allow for CRUD and other advanced operations of flight-related items, including countries, cities, airports, airplanes (incl. their manufacturers) and routes. 

> Currently a work in progress

## Application stack (at the moment)
- http4s
- doobie
- circe
- flyway

## Requirements (at the moment)
- sbt
- JDK 19
- PostgreSQL 15

## Future plans
- Include a Kafka-based queue of current flight status
- An FS2/Kafka-Streams based analysis of flight status
  - with stateful operations
  - and persistence for safe recovery upon failure
- Codebase for a visualization frontend
