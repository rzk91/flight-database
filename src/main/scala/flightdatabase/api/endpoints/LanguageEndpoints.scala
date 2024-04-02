package flightdatabase.api.endpoints

import cats._
import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.model.FlightDbTable.LANGUAGE
import flightdatabase.model.objects.Language
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class LanguageEndpoints[F[_]: Concurrent] private (prefix: String)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => getLanguages.execute.flatMap(toResponse(_))
        case _                  => getStringList(LANGUAGE).execute.flatMap(toResponse(_))
      }

    case req @ POST -> Root =>
      req
        .attemptAs[Language]
        .foldF[ApiResult[Language]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
          language => insertLanguage(language).execute
        )
        .flatMap(toResponse(_))
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String)(
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): HttpRoutes[F] = new LanguageEndpoints(prefix).routes
}
