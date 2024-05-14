package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.country.CountryCreate
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.language.Language
import flightdatabase.utils.implicits.enrichString
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

    // GET /countries?return-only={country_field}
    case GET -> Root :? ReturnOnlyMatcher(returnOnly) =>
      processReturnOnly[Country](returnOnly)(
        stringF = algebra.getCountriesOnly,
        intF = algebra.getCountriesOnly,
        longF = algebra.getCountriesOnly,
        boolF = algebra.getCountriesOnly,
        bigDecimalF = algebra.getCountriesOnly,
        allF = algebra.getCountries
      )

    // GET /countries/filter?field={country_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Country, Country](field, operator, values)(
        stringF = algebra.getCountriesBy,
        intF = algebra.getCountriesBy,
        longF = algebra.getCountriesBy,
        boolF = algebra.getCountriesBy,
        bigDecimalF = algebra.getCountriesBy
      )

    // GET /countries/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getCountry)

    // GET /countries/language/filter?field={language_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "language" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Language, Country](field, operator, values)(
        stringF = algebra.getCountriesByLanguage,
        intF = algebra.getCountriesByLanguage,
        longF = algebra.getCountriesByLanguage,
        boolF = algebra.getCountriesByLanguage,
        bigDecimalF = algebra.getCountriesByLanguage
      )

    // GET /countries/currency/filter?field={currency_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "currency" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Currency, Country](field, operator, values)(
        stringF = algebra.getCountriesByCurrency,
        intF = algebra.getCountriesByCurrency,
        longF = algebra.getCountriesByCurrency,
        boolF = algebra.getCountriesByCurrency,
        bigDecimalF = algebra.getCountriesByCurrency
      )

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
