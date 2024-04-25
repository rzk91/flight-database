package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageCreate
import flightdatabase.domain.language.LanguagePatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class LanguageEndpoints[F[_]: Concurrent] private (prefix: String, algebra: LanguageAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /languages/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesLanguageExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /languages?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getLanguagesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getLanguages.flatMap(toResponse(_))
      }

    // GET /languages/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getLanguage(id).flatMap(toResponse(_)))

    // GET /languages/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getLanguages("name", name).flatMap(toResponse(_))

    // GET /languages/iso/{iso2} OR
    // GET /languages/iso/{iso3}
    case GET -> Root / "iso" / iso =>
      algebra
        .getLanguages("iso2", iso)
        .flatMap {
          case Left(EntryListEmpty) => algebra.getLanguages("iso3", iso)
          case Left(error)          => error.elevate[F, List[Language]]
          case Right(list)          => list.elevate[F]
        }
        .flatMap(toResponse(_))

    // POST /languages
    case req @ POST -> Root =>
      req
        .attemptAs[LanguageCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createLanguage
        )
        .flatMap(toResponse(_))

    // PUT /languages/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Language]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            lang =>
              if (id != lang.id) {
                InconsistentIds(id, lang.id).elevate[F, Long]
              } else {
                algebra.updateLanguage(lang)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /languages/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[LanguagePatch]
          .foldF[ApiResult[Language]](
            _ => EntryInvalidFormat.elevate[F, Language],
            algebra.partiallyUpdateLanguage(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /languages/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeLanguage(id).flatMap(toResponse(_)))
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F]): Endpoints[F] =
    new LanguageEndpoints(prefix, algebra)
}
