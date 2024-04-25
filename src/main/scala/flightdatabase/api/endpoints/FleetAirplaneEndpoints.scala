package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.domain.fleet_airplane.FleetAirplaneAlgebra
import flightdatabase.domain.fleet_airplane.FleetAirplaneCreate
import flightdatabase.domain.fleet_airplane.FleetAirplanePatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class FleetAirplaneEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: FleetAirplaneAlgebra[F]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /fleet-airplanes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesFleetAirplaneExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /fleet-airplanes
    case GET -> Root =>
      algebra.getFleetAirplanes.flatMap(toResponse(_))

    // GET /fleet-airplanes/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getFleetAirplane(id).flatMap(toResponse(_)))

    // GET /fleet-airplanes/fleet/{fleet_id}/airplane/{airplane_id}
    case GET -> Root / "fleet" / fleetId / "airplane" / airplaneId =>
      (fleetId.asLong, airplaneId.asLong).tupled.fold {
        BadRequest(EntryInvalidFormat.error)
      } {
        case (fId, aId) =>
          algebra.getFleetAirplane(fId, aId).flatMap(toResponse(_))
      }

    // GET /fleet-airplanes/airplane/{airplane_id} OR
    // GET /fleet-airplanes/airplane/{airplane_name}
    case GET -> Root / "airplane" / airplane => {
        airplane.asLong.fold {
          // Treat airplane as name
          algebra.getFleetAirplanesByAirplaneName(airplane)
        }(algebra.getFleetAirplanes("airplane_id", _))
      }.flatMap(toResponse(_))

    // GET /fleet-airplanes/fleet/{fleet_id} OR
    // GET /fleet-airplanes/fleet/{fleet_name} OR
    // GET /fleet-airplanes/fleet/{fleet_iso2}
    case GET -> Root / "fleet" / fleet => {
        fleet.asLong.fold {
          // Treat fleet as name
          algebra.getFleetAirplanesByFleetName(fleet).flatMap {
            case Left(EntryListEmpty) =>
              algebra.getFleetAirplanesByExternal[Fleet, String]("iso2", fleet)
            case Left(error) => error.elevate[F, List[FleetAirplane]]
            case Right(list) => list.elevate[F]
          }
        }(algebra.getFleetAirplanes("fleet_id", _))
      }.flatMap(toResponse(_))

    // POST /fleet-airplanes
    case req @ POST -> Root =>
      req
        .attemptAs[FleetAirplaneCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createFleetAirplane
        )
        .flatMap(toResponse(_))

    // PUT /fleet-airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[FleetAirplane]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            fa =>
              if (id != fa.id) {
                InconsistentIds(id, fa.id).elevate[F, Long]
              } else {
                algebra.updateFleetAirplane(fa)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /fleet-airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[FleetAirplanePatch]
          .foldF[ApiResult[FleetAirplane]](
            _ => EntryInvalidFormat.elevate[F, FleetAirplane],
            algebra.partiallyUpdateFleetAirplane(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /fleet-airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeFleetAirplane(id).flatMap(toResponse(_)))
  }
}

object FleetAirplaneEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: FleetAirplaneAlgebra[F]): Endpoints[F] =
    new FleetAirplaneEndpoints(prefix, algebra)
}
