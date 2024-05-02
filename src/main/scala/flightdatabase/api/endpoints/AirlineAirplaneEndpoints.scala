package flightdatabase.api.endpoints

import cats.effect._
import cats.syntax.flatMap._
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate
import flightdatabase.domain.airplane.Airplane
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineAirplaneEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineAirplaneAlgebra[F]
) extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-airplanes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineAirplaneExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-airplanes
    case GET -> Root =>
      algebra.getAirlineAirplanes.flatMap(toResponse(_))

    // GET /airline-airplanes/{id}
    case GET -> Root / id =>
      idToResponse(id)(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / airlineId / "airplane" / airplaneId =>
      idsToResponse(airlineId, airplaneId)(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/{value}?field={airline_field; default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Airline](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(
            algebra.getAirlineAirplanesByAirline(field, _)
          )
        } else {
          algebra.getAirlineAirplanesByAirline(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airline-airplanes/airplane/{value}?field={airplane_field; default: id}
    case GET -> Root / "airplane" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Airplane](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(
            algebra.getAirlineAirplanesByAirplane(field, _)
          )
        } else {
          algebra.getAirlineAirplanesByAirplane(field, value).flatMap(toResponse(_))
        }
      }

    // POST /airline-airplanes
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirlineAirplane).flatMap(toResponse(_))

    // PUT /airline-airplanes/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[AirlineAirplaneCreate, Long](req) { aa =>
          if (aa.id.exists(_ != i)) {
            InconsistentIds(i, aa.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineAirplane(AirlineAirplane.fromCreate(i, aa))
          }
        }
      }

    // PATCH /airline-airplanes/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirlineAirplane(i, _)))

    // DELETE /airline-airplanes/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirlineAirplane)
  }
}

object AirlineAirplaneEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAirplaneAlgebra[F]): Endpoints[F] =
    new AirlineAirplaneEndpoints(prefix, algebra)
}
