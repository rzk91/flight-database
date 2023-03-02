package flightdatabase.api

import cats.effect._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.model._
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

  object LanguageNameQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[Boolean]("only-names")

  val flightDbService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "countries" => getStringList("country").runStmt.flatMap(toResponse)

    case GET -> Root / "cities" :? CountryQueryParamMatcher(maybeCountry) =>
      getCityNames(maybeCountry).runStmt.flatMap(toResponse)

    case GET -> Root / "languages" :? LanguageNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => getLanguages.runStmt.flatMap(toResponse)
        case _                  => getStringList("language").runStmt.flatMap(toResponse)
      }

    case req @ POST -> Root / "languages" =>
      req
        .attemptAs[Language]
        .foldF[ApiResult[Language]](
          _ => IO(Left(EntryInvalidFormat)),
          language => insertLanguage(language).runStmt
        )
        .flatMap(toResponse)
  }
}

// Created(
//   v,
//   Location(Uri.unsafeFromString(s"/flightdb/languages/${v.id.get}"))
// )
