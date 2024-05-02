package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityAlgebra
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
      idToResponse(id)(algebra.getAirlineCity)

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      idsToResponse(airlineId, cityId)(algebra.getAirlineCity)

    // GET /airline-cities/airline/{value}?field={airline_field, default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlineCitiesByAirline(field, _))
      } else {
        algebra.getAirlineCitiesByAirline(field, value).flatMap(toResponse(_))
      }

    // GET /airline-cities/city/{value}?field={city_field, default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlineCitiesByCity(field, _))
      } else {
        algebra.getAirlineCitiesByCity(field, value).flatMap(toResponse(_))
      }

    // POST /airline-cities
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirlineCity).flatMap(toResponse(_))

    // PUT /airline-cities/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[AirlineCity, Long](req) { airlineCity =>
          if (i != airlineCity.id) {
            InconsistentIds(i, airlineCity.id).elevate[F, Long]
          } else {
            algebra.updateAirlineCity(airlineCity)
          }
        }
      }

    // PATCH /airline-cities/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirlineCity(i, _)))

    // DELETE /airline-cities/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirlineCity)
  }
}

object AirlineCityEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineCityAlgebra[F]): Endpoints[F] =
    new AirlineCityEndpoints(prefix, algebra)
}
