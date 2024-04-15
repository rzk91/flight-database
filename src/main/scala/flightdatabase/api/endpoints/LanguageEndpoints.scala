package flightdatabase.api.endpoints

import cats._
import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageModel
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class LanguageEndpoints[F[_]: Concurrent] private (prefix: String, algebra: LanguageAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getLanguagesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getLanguages.flatMap(toResponse(_))
      }

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

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F]): Endpoints[F] =
    new LanguageEndpoints(prefix, algebra)
}
