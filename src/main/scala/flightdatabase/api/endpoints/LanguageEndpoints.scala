package flightdatabase.api.endpoints

import cats.effect._
import cats.syntax.flatMap._
import flightdatabase.domain._
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageCreate
import flightdatabase.utils.implicits.enrichString
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
        algebra.getLanguagesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getLanguages.flatMap(_.toResponse)
      }

    // GET /languages/search?field={language_field}&value={value}&condition_type={operator; default: eq}
    // https://developer.adobe.com/commerce/webapi/rest/use-rest/performing-searches/

    // GET /languages/{value}?field={language_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getLanguage)
      } else {
        implicitly[TableBase[Language]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getLanguages(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getLanguages(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getLanguages(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getLanguages(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getLanguages(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // POST /languages
    case req @ POST -> Root =>
      processRequest(req)(algebra.createLanguage).flatMap(_.toResponse)

    // PUT /languages/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[LanguageCreate, Long](req) { language =>
          if (language.id.exists(_ != i)) {
            InconsistentIds(i, language.id.get).elevate[F, Long]
          } else {
            algebra.updateLanguage(Language.fromCreate(i, language))
          }
        }
      }

    // PATCH /languages/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateLanguage(i, _)))

    // DELETE /languages/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeLanguage)
  }
}

object LanguageEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: LanguageAlgebra[F]): Endpoints[F] =
    new LanguageEndpoints(prefix, algebra)
}
