package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.country.CountryCreate
import flightdatabase.domain.country.CountryPatch
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
        algebra.getCountriesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getCountries.flatMap(toResponse(_))
      }

    // GET /countries/{value}?field={country_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id"                      => algebra.getCountry(safeId).flatMap(toResponse(_))
        case str if str.endsWith("id") => algebra.getCountries(field, safeId).flatMap(toResponse(_))
        case _                         => algebra.getCountries(field, value).flatMap(toResponse(_))
      }

    // GET /countries/language/{value}?field={language_field, default: id}
    case GET -> Root / "language" / value :? FieldMatcherIdDefault(field) =>
      field match {
        case "id" =>
          val safeId = value.asLong.getOrElse(-1L)
          algebra.getCountriesByLanguage(field, safeId).flatMap(toResponse(_))
        case _ => algebra.getCountriesByLanguage(field, value).flatMap(toResponse(_))
      }

    // GET /countries/currency/{value}?field={currency_field, default: id}
    case GET -> Root / "currency" / value :? FieldMatcherIdDefault(field) =>
      field match {
        case "id" =>
          val safeId = value.asLong.getOrElse(-1L)
          algebra.getCountriesByCurrency(field, safeId).flatMap(toResponse(_))
        case _ => algebra.getCountriesByCurrency(field, value).flatMap(toResponse(_))
      }

    // POST /countries
    case req @ POST -> Root =>
      req
        .attemptAs[CountryCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createCountry
        )
        .flatMap(toResponse(_))

    // PUT /countries/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Country]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            country =>
              if (id != country.id) {
                InconsistentIds(id, country.id).elevate[F, Long]
              } else {
                algebra.updateCountry(country)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /countries/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[CountryPatch]
          .foldF[ApiResult[Country]](
            _ => EntryInvalidFormat.elevate[F, Country],
            algebra.partiallyUpdateCountry(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /countries/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeCountry(id).flatMap(toResponse(_)))
  }
}

object CountryEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CountryAlgebra[F]
  ): Endpoints[F] =
    new CountryEndpoints(prefix, algebra)
}
