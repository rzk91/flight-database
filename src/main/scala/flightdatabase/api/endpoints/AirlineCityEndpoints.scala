package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityAlgebra
import flightdatabase.domain.airline_city.AirlineCityCreate
import flightdatabase.domain.airline_city.AirlineCityPatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineCityEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineCityAlgebra[F]
) extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-cities/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineCityExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-cities
    case GET -> Root =>
      algebra.getAirlineCities.flatMap(toResponse(_))

    // GET /airline-cities/{id}
    case GET -> Root / id =>
      lazy val safeId = id.asLong.getOrElse(-1L)
      algebra.getAirlineCity(safeId).flatMap(toResponse(_))

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      lazy val safeAirlineId = airlineId.asLong.getOrElse(-1L)
      lazy val safeCityId = cityId.asLong.getOrElse(-1L)
      algebra.getAirlineCity(safeAirlineId, safeCityId).flatMap(toResponse(_))

    // GET /airline-cities/airline/{value}?field={airline_field, default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      if (field.endsWith("id")) {
        algebra.getAirlineCitiesByAirline(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getAirlineCitiesByAirline(field, value).flatMap(toResponse(_))
      }

    // GET /airline-cities/city/{value}?field={city_field, default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      if (field.endsWith("id")) {
        algebra.getAirlineCitiesByCity(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getAirlineCitiesByCity(field, value).flatMap(toResponse(_))
      }

    // POST /airline-cities
    case req @ POST -> Root =>
      req
        .attemptAs[AirlineCityCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirlineCity
        )
        .flatMap(toResponse(_))

    // PUT /airline-cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineCity]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            airlineCity =>
              if (id != airlineCity.id) {
                InconsistentIds(id, airlineCity.id).elevate[F, Long]
              } else {
                algebra.updateAirlineCity(airlineCity)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airline-cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineCityPatch]
          .foldF[ApiResult[AirlineCity]](
            _ => EntryInvalidFormat.elevate[F, AirlineCity],
            algebra.partiallyUpdateAirlineCity(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airline-cities/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirlineCity(id).flatMap(toResponse(_)))
  }
}

object AirlineCityEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineCityAlgebra[F]): Endpoints[F] =
    new AirlineCityEndpoints(prefix, algebra)
}
