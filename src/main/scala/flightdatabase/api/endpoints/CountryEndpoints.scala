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

    // GET /countries?onlyNames
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getCountriesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getCountries.flatMap(_.toResponse)
      }

    // GET /countries/{value}?field={country_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getCountry)
      } else {
        implicitly[TableBase[Country]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getCountries(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getCountries(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getCountries(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getCountries(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getCountries(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // GET /countries/language/{value}?field={language_field, default: id}
    case GET -> Root / "language" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Language]].fieldTypeMap.get(field) match {
        case Some(StringType) => algebra.getCountriesByLanguage(field, value).flatMap(_.toResponse)
        case Some(IntType)    => value.asInt.toResponse(algebra.getCountriesByLanguage(field, _))
        case Some(LongType)   => value.asLong.toResponse(algebra.getCountriesByLanguage(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getCountriesByLanguage(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getCountriesByLanguage(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /countries/currency/{value}?field={currency_field, default: id}
    case GET -> Root / "currency" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Currency]].fieldTypeMap.get(field) match {
        case Some(StringType) => algebra.getCountriesByCurrency(field, value).flatMap(_.toResponse)
        case Some(IntType)    => value.asInt.toResponse(algebra.getCountriesByCurrency(field, _))
        case Some(LongType)   => value.asLong.toResponse(algebra.getCountriesByCurrency(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getCountriesByCurrency(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getCountriesByCurrency(field, _))
        case None => BadRequest(InvalidField(field).error)
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
