package flightdatabase.api

import cats.effect._
import doobie.implicits._
import flightdatabase.db.DbMethods._
import org.http4s._
import org.http4s.dsl.io._

object ApiEndpoints {

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name! Check out our amazing flight database!")
  }

  object CountryQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("country")

  val flightDbService = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => runQuery(getCountryNames).flatMap(Ok(_))
    case GET -> Root / "cities" :? CountryQueryParamMatcher(maybeCountry) =>
      runQuery(getCityNames(maybeCountry)).flatMap(Ok(_))
  }
}
