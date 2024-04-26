package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate
import flightdatabase.domain.airline_airplane.AirlineAirplanePatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineAirplaneEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineAirplaneAlgebra[F]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
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
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getAirlineAirplane(id).flatMap(toResponse(_)))

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / airlineId / "airplane" / airplaneId =>
      (airlineId.asLong, airplaneId.asLong).tupled.fold {
        BadRequest(EntryInvalidFormat.error)
      } {
        case (fId, aId) =>
          algebra.getAirlineAirplane(fId, aId).flatMap(toResponse(_))
      }

    // TODO: Use query params instead of path params
    // GET /airline-airplanes/airplane/{airplane_id} OR
    // GET /airline-airplanes/airplane/{airplane_name}
    case GET -> Root / "airplane" / airplane => {
        airplane.asLong.fold {
          // Treat airplane as name
          algebra.getAirlineAirplanesByAirplaneName(airplane)
        }(algebra.getAirlineAirplanes("airplane_id", _))
      }.flatMap(toResponse(_))

    // TODO: Use query params instead of path params
    // GET /airline-airplanes/airline/{airline_id} OR
    // GET /airline-airplanes/airline/{airline_name} OR
    // GET /airline-airplanes/airline/{airline_iso2}
    case GET -> Root / "airline" / airline => {
        airline.asLong.fold {
          // Treat airline as name
          algebra.getAirlineAirplanesByAirlineName(airline).flatMap {
            case Left(EntryListEmpty) =>
              algebra.getAirlineAirplanesByExternal[Airline, String]("iso2", airline)
            case Left(error) => error.elevate[F, List[AirlineAirplane]]
            case Right(list) => list.elevate[F]
          }
        }(algebra.getAirlineAirplanes("airline_id", _))
      }.flatMap(toResponse(_))

    // POST /airline-airplanes
    case req @ POST -> Root =>
      req
        .attemptAs[AirlineAirplaneCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirlineAirplane
        )
        .flatMap(toResponse(_))

    // PUT /airline-airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineAirplane]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            fa =>
              if (id != fa.id) {
                InconsistentIds(id, fa.id).elevate[F, Long]
              } else {
                algebra.updateAirlineAirplane(fa)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airline-airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineAirplanePatch]
          .foldF[ApiResult[AirlineAirplane]](
            _ => EntryInvalidFormat.elevate[F, AirlineAirplane],
            algebra.partiallyUpdateAirlineAirplane(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airline-airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirlineAirplane(id).flatMap(toResponse(_)))
  }
}

object AirlineAirplaneEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAirplaneAlgebra[F]): Endpoints[F] =
    new AirlineAirplaneEndpoints(prefix, algebra)
}
