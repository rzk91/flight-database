package flightdatabase.api.endpoints

import cats._
import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageModel
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class LanguageEndpoints[F[_]: Concurrent] private (prefix: String, algebra: LanguageAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => algebra.getLanguages.flatMap(toResponse(_))
        case _                  => algebra.getLanguagesOnlyNames.flatMap(toResponse(_))
      }

    // TODO: Refactor (move actual logic to `LanguageRepository`)
    case req @ POST -> Root =>
      req
        .attemptAs[LanguageModel]
        .foldF[ApiResult[Long]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
          algebra.createLanguage
        )
        .flatMap(toResponse(_))
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F])(
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): HttpRoutes[F] = new LanguageEndpoints(prefix, algebra).routes
}
