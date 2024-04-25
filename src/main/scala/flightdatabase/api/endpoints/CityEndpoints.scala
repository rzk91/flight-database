package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityCreate
import flightdatabase.domain.city.CityPatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CityEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CityAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {

    // HEAD /cities/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesCityExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /cities?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getCitiesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getCities.flatMap(toResponse(_))
      }

    // GET /cities/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getCity(id).flatMap(toResponse(_)))

    // GET /cities/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getCities("name", name).flatMap(toResponse(_))

    // GET /cities/country/{country_name} OR
    // GET /cities/country/{country_id}
    case GET -> Root / "country" / country =>
      country.asLong.fold[F[Response[F]]] {
        // Treat country as name
        algebra.getCitiesByCountry(country).flatMap(toResponse(_))
      }(algebra.getCities("country_id", _).flatMap(toResponse(_)))

    // POST /cities
    case req @ POST -> Root =>
      req
        .attemptAs[CityCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createCity
        )
        .flatMap(toResponse(_))

    // PUT /cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[City]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            city =>
              if (id != city.id) {
                InconsistentIds(id, city.id).elevate[F, Long]
              } else {
                algebra.updateCity(city)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[CityPatch]
          .foldF[ApiResult[City]](
            _ => EntryInvalidFormat.elevate[F, City],
            algebra.partiallyUpdateCity(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /cities/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeCity(id).flatMap(toResponse(_)))
  }
}

object CityEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CityAlgebra[F]
  ): Endpoints[F] =
    new CityEndpoints(prefix, algebra)
}
