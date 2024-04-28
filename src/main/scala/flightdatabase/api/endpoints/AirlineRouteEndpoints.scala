package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.api.toResponse
import flightdatabase.domain._
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteAlgebra
import flightdatabase.domain.airline_route.AirlineRouteCreate
import flightdatabase.domain.airline_route.AirlineRoutePatch
import flightdatabase.utils.implicits.enrichString
import flightdatabase.utils.implicits.stringToRichIterable
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineRouteEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineRouteAlgebra[F]
) extends Endpoints[F](prefix) {

  private object OnlyNumbersFlagMatcher extends FlagQueryParamMatcher("only-routes")
  private object InboundFlagMatcher extends FlagQueryParamMatcher("inbound")
  private object OutboundFlagMatcher extends FlagQueryParamMatcher("outbound")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-routes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineRouteExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-routes?only-routes
    case GET -> Root :? OnlyNumbersFlagMatcher(onlyRoutes) =>
      if (onlyRoutes) {
        algebra.getAirlineRoutesOnlyRoutes.flatMap(toResponse(_))
      } else {
        algebra.getAirlineRoutes.flatMap(toResponse(_))
      }

    // TODO: Rewrite the following routes based on recent changes in repository
    // GET /airline-routes/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getAirlineRoute(id).flatMap(toResponse(_)))

    // GET /airline-routes/route/{route_number}
    case GET -> Root / "route" / routeNumber =>
      algebra.getAirlineRoutes("route_number", routeNumber).flatMap(toResponse(_))

    // GET /airline-routes/airline/{airline_id}
    case GET -> Root / "airline" / airlineId =>
      airlineId.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(airlineId => algebra.getAirlineRoutesByAirlineId(airlineId).flatMap(toResponse(_)))

    // GET /airline-routes/airplane/{airplane_id}
    case GET -> Root / "airplane" / airplaneId =>
      airplaneId.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(airplaneId => algebra.getAirlineRoutesByAirplaneId(airplaneId).flatMap(toResponse(_)))

    // GET /airline-routes/airport/{airport_id_or_iata_or_icao}?inbound&outbound
    case GET -> Root / "airport" / airportIdOrIataOrIcao :?
          InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) => {
        airportIdOrIataOrIcao.asLong match {
          case Some(airportId) =>
            algebra
              .getAirlineRoutesByAirport(
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
              .getAirlineRoutesByAirport(
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

    // POST /airline-routes
    case req @ POST -> Root =>
      req
        .attemptAs[AirlineRouteCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirlineRoute
        )
        .flatMap(toResponse(_))

    // PUT /airline-routes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineRoute]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            airlineRoute =>
              if (id != airlineRoute.id) {
                InconsistentIds(id, airlineRoute.id).elevate[F, Long]
              } else {
                algebra.updateAirlineRoute(airlineRoute)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airline-routes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlineRoutePatch]
          .foldF[ApiResult[AirlineRoute]](
            _ => EntryInvalidFormat.elevate[F, AirlineRoute],
            algebra.partiallyUpdateAirlineRoute(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airline-routes/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirlineRoute(id).flatMap(toResponse(_)))
  }
}

object AirlineRouteEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineRouteAlgebra[F]): Endpoints[F] =
    new AirlineRouteEndpoints(prefix, algebra)
}
