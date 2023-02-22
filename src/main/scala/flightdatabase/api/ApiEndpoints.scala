package flightdatabase.api

import cats.effect._
import doobie.implicits._
import flightdatabase.db.DbMethods._
import org.http4s._
import org.http4s.dsl.io._

object ApiEndpoints {

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name")
  }

  val flightDbService = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => runQuery(getCountryNames).flatMap(Ok(_))
    case GET -> Root / "cities" / "bycountry" / country =>
      runQuery(getCitiesFromCountry(country)).flatMap(Ok(_))
  }
}
