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

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {

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

    // GET /countries/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getCountry(id).flatMap(toResponse(_)))

    // GET /countries/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getCountries("name", name).flatMap(toResponse(_))

    // GET /countries/language/{value}?field={iso2, default: name}
    case GET -> Root / "language" / value :? FieldMatcherWithDefaultName(field) =>
      value.asLong
        .fold(algebra.getCountriesByLanguage(field, value)) { long =>
          algebra.getCountriesByLanguage("id", long)
        }
        .flatMap(toResponse(_))

    // GET /countries/currency/{value}?field={iso, default: name}
    case GET -> Root / "currency" / value :? FieldMatcherWithDefaultName(field) =>
      value.asLong
        .fold(algebra.getCountriesByCurrency(field, value)) { long =>
          algebra.getCountriesByCurrency("id", long)
        }
        .flatMap(toResponse(_))

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
