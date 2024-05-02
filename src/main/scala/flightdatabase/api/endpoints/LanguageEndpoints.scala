package flightdatabase.api.endpoints

import cats.effect._
import cats.syntax.flatMap._
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageAlgebra
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class LanguageEndpoints[F[_]: Concurrent] private (prefix: String, algebra: LanguageAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /languages/{value}?field={language_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Language](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getLanguage)
          case _    => algebra.getLanguages(field, value).flatMap(toResponse(_))
        }
      }

    // POST /languages
    case req @ POST -> Root =>
      processRequest(req)(algebra.createLanguage).flatMap(toResponse(_))

    // PUT /languages/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Language, Long](req) { language =>
          if (i != language.id) {
            InconsistentIds(i, language.id).elevate[F, Long]
          } else {
            algebra.updateLanguage(language)
          }
        }
      }

    // PATCH /languages/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateLanguage(i, _)))

    // DELETE /languages/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeLanguage)
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F]): Endpoints[F] =
    new LanguageEndpoints(prefix, algebra)
}
