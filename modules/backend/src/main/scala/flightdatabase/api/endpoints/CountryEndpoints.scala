package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase._
import flightdatabase.country.Country
import flightdatabase.country.CountryAlgebra
import flightdatabase.country.CountryCreate
import flightdatabase.currency.Currency
import flightdatabase.extensions.string._
import flightdatabase.language.Language
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

    // GET /countries?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processReturnOnly2[Country](_, returnOnly)(algebra.getCountries)
      }

    // GET /countries/filter?field={country_field}&operator={operator; default: eq}&value={value}&sort-by={country_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processFilter2[Country, Country](field, operator, values, _)(algebra.getCountriesBy)
      }

    // GET /countries/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getCountry)

    // GET /countries/language/filter?field={language_field}&operator={operator; default: eq}&value={value}&sort-by={language_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "language" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Language](sortAndLimit) {
        processFilter2[Language, Country](field, operator, values, _)(algebra.getCountriesByLanguage)
      }

    // GET /countries/currency/filter?field={currency_field}&operator={operator; default: eq}&value={value}&sort-by={currency_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "currency" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Currency](sortAndLimit) {
        processFilter2[Currency, Country](field, operator, values, _)(algebra.getCountriesByCurrency)
      }

    // POST /countries
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createCountry).flatMap(_.toResponse)

    // PUT /countries/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[CountryCreate, Long](req) { country =>
          if (country.id.exists(_ != i)) {
            InconsistentIds(i, country.id.get).elevate[F, Long]
          } else {
            algebra.updateCountry(Country.fromCreate(i, country))
          }
        }
      }

    // PATCH /countries/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateCountry(i, _)))

    // DELETE /countries/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeCountry)
  }
}

object CountryEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CountryAlgebra[F]
  ): Endpoints[F] =
    new CountryEndpoints(prefix, algebra)
}
