package flightdatabase.api

import cats.effect._
import doobie.implicits._
import flightdatabase.db.DbMethods._
import flightdatabase.model.objects._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.headers.Location

object ApiEndpoints {

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name! Check out our amazing flight database!")
  }

  object CountryQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("country")

  val flightDbService = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => runStmt(getCountryNames).flatMap(Ok(_))
    case GET -> Root / "cities" :? CountryQueryParamMatcher(maybeCountry) =>
      runStmt(getCityNames(maybeCountry)).flatMap(Ok(_))
    case req @ POST -> Root / "languages" =>
      for {
        lang    <- req.decodeJson[Language]
        created <- runStmt(insertLanguage(lang))
        response <- Created(
          created,
          Location(Uri.unsafeFromString(s"/flightdb/languages/${created.id.get}"))
        )
      } yield response
  }
}
