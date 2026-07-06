package flightdatabase.api.endpoints

import cats.effect._
import cats.syntax.flatMap._
import flightdatabase._
import flightdatabase.language.Language
import flightdatabase.language.LanguageAlgebra
import flightdatabase.language.LanguageCreate
import flightdatabase.syntax.string._
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

    // GET /languages?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Language](sortAndLimit) {
        processReturnOnly[Language](_, returnOnly)(algebra.getLanguages)
      }

    // GET /languages/filter?field={language_field}&operator={operator; default: eq}&value={value}&sort-by={language_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
        FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
        ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Language](sortAndLimit) {
        processFilter[Language, Language](field, operator, values, _)(algebra.getLanguagesBy)
      }

    // GET /languages/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getLanguage)

    // POST /languages
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createLanguage).flatMap(_.toResponse)

    // PUT /languages/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[LanguageCreate, Long](req) { language =>
          if (language.id.exists(_ != i)) {
            InconsistentIds(i, language.id.get).elevate[F, Long]
          } else {
            algebra.updateLanguage(Language.fromCreate(i, language))
          }
        }
      }

    // PATCH /languages/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateLanguage(i, _)))

    // DELETE /languages/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeLanguage)
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F]): Endpoints[F] =
    new LanguageEndpoints(prefix, algebra)
}
