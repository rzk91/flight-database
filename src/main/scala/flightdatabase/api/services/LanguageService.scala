package flightdatabase.api.services

import cats._
import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.model._
import flightdatabase.model.objects.Language
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

class LanguageService[F[_]: Async](implicit F: Applicative[F]) extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "languages" :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => getLanguages.execute.flatMap(toResponse[F, List[Language]])
        case _                  => getStringList("language").execute.flatMap(toResponse[F, List[String]])
      }

    case req @ POST -> Root / "languages" =>
      req
        .attemptAs[Language]
        .foldF[ApiResult[Language]](
          _ => F.pure(Left(EntryInvalidFormat)),
          language => insertLanguage(language).execute
        )
        .flatMap(toResponse[F, Language])
  }
}

object LanguageService {

  def apply[F[_]: Async](implicit F: Applicative[F]): HttpRoutes[F] = new LanguageService[F].service
}
