package flightdatabase.api

import cats.effect._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.model.objects._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.headers.Location

object ApiEndpoints {

  val helloWorldService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name! Check out our amazing flight database!")
  }

  object CountryQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("country")

  val flightDbService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => getCountryNames.runStmt.flatMap(Ok(_))

    case GET -> Root / "cities" :? CountryQueryParamMatcher(maybeCountry) =>
      getCityNames(maybeCountry).runStmt.flatMap(Ok(_))

    case GET -> Root / "languages" => getLanguages.runStmt.flatMap(Ok(_))

    // FixMe!
    case req @ POST -> Root / "languages" =>
      req
        .attemptAs[Language]
        .foldF(
          _ => BadRequest("Invalid input"),
          language =>
            insertLanguage(language) match {
              case Some(out) => for {
                created <- out.runStmt
                response <- Created(
                  created,
                  Location(Uri.unsafeFromString(s"/flightdb/languages/${created.id.get}"))
                )
              } yield response
              case None => Conflict()
            }
        )
  }
}
