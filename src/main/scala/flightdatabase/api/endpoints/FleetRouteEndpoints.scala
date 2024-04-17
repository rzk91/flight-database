package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.api.toResponse
import flightdatabase.domain._
import flightdatabase.domain.fleet_route.FleetRoute
import flightdatabase.domain.fleet_route.FleetRouteAlgebra
import flightdatabase.domain.fleet_route.FleetRouteCreate
import flightdatabase.domain.fleet_route.FleetRoutePatch
import flightdatabase.utils.implicits.enrichString
import flightdatabase.utils.implicits.stringToRichIterable
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class FleetRouteEndpoints[F[_]: Concurrent] private (prefix: String, algebra: FleetRouteAlgebra[F])
    extends Endpoints[F](prefix) {

  private object OnlyNumbersFlagMatcher extends FlagQueryParamMatcher("only-routes")
  private object InboundFlagMatcher extends FlagQueryParamMatcher("inbound")
  private object OutboundFlagMatcher extends FlagQueryParamMatcher("outbound")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /fleet-routes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesFleetRouteExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /fleet-routes?only-routes
    case GET -> Root :? OnlyNumbersFlagMatcher(onlyRoutes) =>
      if (onlyRoutes) {
        algebra.getFleetRoutesOnlyRoutes.flatMap(toResponse(_))
      } else {
        algebra.getFleetRoutes.flatMap(toResponse(_))
      }

    // GET /fleet-routes/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getFleetRoute(id).flatMap(toResponse(_)))

    // GET /fleet-routes/route/{route_number}
    case GET -> Root / "route" / routeNumber =>
      algebra.getFleetRoutes("route_number", routeNumber).flatMap(toResponse(_))

    // GET /fleet-routes/fleet/{fleet_id}
    case GET -> Root / "fleet" / fleetId =>
      fleetId.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(fleetId => algebra.getFleetRoutesByFleetId(fleetId).flatMap(toResponse(_)))

    // GET /fleet-routes/airplane/{airplane_id}
    case GET -> Root / "airplane" / airplaneId =>
      airplaneId.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(airplaneId => algebra.getFleetRoutesByAirplaneId(airplaneId).flatMap(toResponse(_)))

    // GET /fleet-routes/airport/{airport_id_or_iata_or_icao}?inbound&outbound
    case GET -> Root / "airport" / airportIdOrIataOrIcao :?
          InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) => {
        airportIdOrIataOrIcao.asLong match {
          case Some(airportId) =>
            algebra
              .getFleetRoutesByAirport(
                "airport_id",
                airportId,
                (inbound, outbound) match {
                  case (true, false) => Some(true)
                  case (false, true) => Some(false)
                  case _             => None
                }
              )
          case None =>
            algebra
              .getFleetRoutesByAirport(
                if (airportIdOrIataOrIcao.lengthEquals(3)) "iata" else "icao",
                airportIdOrIataOrIcao,
                (inbound, outbound) match {
                  case (true, false) => Some(true)
                  case (false, true) => Some(false)
                  case _             => None
                }
              )
        }
      }.flatMap(toResponse(_))

    // POST /fleet-routes
    case req @ POST -> Root =>
      req
        .attemptAs[FleetRouteCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createFleetRoute
        )
        .flatMap(toResponse(_))

    // PUT /fleet-routes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[FleetRoute]
          .foldF[ApiResult[FleetRoute]](
            _ => EntryInvalidFormat.elevate[F, FleetRoute],
            fleetRoute =>
              if (id != fleetRoute.id) {
                InconsistentIds(id, fleetRoute.id).elevate[F, FleetRoute]
              } else {
                algebra.updateFleetRoute(fleetRoute)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /fleet-routes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[FleetRoutePatch]
          .foldF[ApiResult[FleetRoute]](
            _ => EntryInvalidFormat.elevate[F, FleetRoute],
            algebra.partiallyUpdateFleetRoute(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /fleet-routes/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeFleetRoute(id).flatMap(toResponse(_)))
  }
}

object FleetRouteEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: FleetRouteAlgebra[F]): Endpoints[F] =
    new FleetRouteEndpoints(prefix, algebra)
}
