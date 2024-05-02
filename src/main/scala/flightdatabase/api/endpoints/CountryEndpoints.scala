package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.language.Language
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CountryEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CountryAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {

    // HEAD /countries/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesCountryExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /countries?onlyNames
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getCountriesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getCountries.flatMap(toResponse(_))
      }

    // GET /countries/{value}?field={country_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Country](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getCountry)
          case str if str.endsWith("id") =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getCountries(field, _))
          case _ => algebra.getCountries(field, value).flatMap(toResponse(_))
        }
      }

    // GET /countries/language/{value}?field={language_field, default: id}
    case GET -> Root / "language" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Language](field) {
        field match {
          case "id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getCountriesByLanguage(field, _))
          case _ => algebra.getCountriesByLanguage(field, value).flatMap(toResponse(_))
        }
      }

    // GET /countries/currency/{value}?field={currency_field, default: id}
    case GET -> Root / "currency" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Currency](field) {
        field match {
          case "id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getCountriesByCurrency(field, _))
          case _ => algebra.getCountriesByCurrency(field, value).flatMap(toResponse(_))
        }
      }

    // POST /countries
    case req @ POST -> Root =>
      processRequest(req)(algebra.createCountry).flatMap(toResponse(_))

    // PUT /countries/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Country, Long](req) { country =>
          if (i != country.id) {
            InconsistentIds(i, country.id).elevate[F, Long]
          } else {
            algebra.updateCountry(country)
          }
        }
      }

    // PATCH /countries/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateCountry(i, _)))

    // DELETE /countries/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeCountry)
  }
}

object CountryEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CountryAlgebra[F]
  ): Endpoints[F] =
    new CountryEndpoints(prefix, algebra)
}
