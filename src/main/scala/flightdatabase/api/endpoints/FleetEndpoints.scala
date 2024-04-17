package flightdatabase.api.endpoints

import cats.Applicative
import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.api.toResponse
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet.FleetAlgebra
import flightdatabase.domain.fleet.FleetCreate
import flightdatabase.domain.fleet.FleetPatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class FleetEndpoints[F[_]: Concurrent] private (prefix: String, algebra: FleetAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /fleets/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesFleetExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /fleets?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getFleetsOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getFleets.flatMap(toResponse(_))
      }

    // GET /fleets/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getFleet(id).flatMap(toResponse(_)))

    // GET /fleets/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getFleets("name", name).flatMap(toResponse(_))

    // GET /fleets/iso2/{iso2}
    case GET -> Root / "iso2" / iso2 =>
      algebra.getFleets("iso2", iso2).flatMap(toResponse(_))

    // GET /fleets/hub-airport/{hubAirportId}
    case GET -> Root / "hub-airport" / LongVar(hubAirportId) =>
      algebra.getFleets("hub_airport_id", hubAirportId).flatMap(toResponse(_))

    // GET /fleets/hub-airport-iata/{hubAirportIata}
    case GET -> Root / "hub-airport-iata" / hubAirportIata =>
      algebra.getFleetByHubAirportIata(hubAirportIata).flatMap(toResponse(_))

    // GET /fleets/hub-airport-icao/{hubAirportIcao}
    case GET -> Root / "hub-airport-icao" / hubAirportIcao =>
      algebra.getFleetByHubAirportIcao(hubAirportIcao).flatMap(toResponse(_))

    // POST /fleets
    case req @ POST -> Root =>
      req
        .attemptAs[FleetCreate]
        .foldF[ApiResult[Long]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
          algebra.createFleet
        )
        .flatMap(toResponse(_))

    // PUT /fleets/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Fleet]
          .foldF[ApiResult[Fleet]](
            _ => Applicative[F].pure(Left(EntryInvalidFormat)),
            fleet =>
              if (id != fleet.id) {
                Applicative[F].pure(Left(InconsistentIds(id, fleet.id)))
              } else {
                algebra.updateFleet(fleet)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /fleets/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[FleetPatch]
          .foldF[ApiResult[Fleet]](
            _ => Applicative[F].pure(Left(EntryInvalidFormat)),
            algebra.partiallyUpdateFleet(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /fleets/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeFleet(id).flatMap(toResponse(_)))
  }
}

object FleetEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: FleetAlgebra[F]): Endpoints[F] =
    new FleetEndpoints[F](prefix, algebra)
}
