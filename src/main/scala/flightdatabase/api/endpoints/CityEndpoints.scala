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

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {

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

    // GET /cities/{value}?field={city_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id"         => algebra.getCity(safeId).flatMap(toResponse(_))
        case "country_id" => algebra.getCities(field, safeId).flatMap(toResponse(_))
        case _            => algebra.getCities(field, value).flatMap(toResponse(_))
      }

    // GET /cities/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        val safeId = value.asLong.getOrElse(-1L)
        algebra.getCitiesByCountry(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getCitiesByCountry(field, value).flatMap(toResponse(_))
      }

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
