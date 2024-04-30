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
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineRouteEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineRouteAlgebra[F]
) extends Endpoints[F](prefix) {

  private object OnlyNumbersFlagMatcher extends FlagQueryParamMatcher("only-routes")
  private object InboundFlagMatcher extends FlagQueryParamMatcher("inbound")
  private object OutboundFlagMatcher extends FlagQueryParamMatcher("outbound")

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /airline-routes/{value}?field={airline-route-field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id" => algebra.getAirlineRoute(safeId).flatMap(toResponse(_))
        case str if str.endsWith("id") =>
          algebra.getAirlineRoutes(field, safeId).flatMap(toResponse(_))
        case _ => algebra.getAirlineRoutes(field, value).flatMap(toResponse(_))
      }

    // GET /airline-routes/airline/{value}?field={airline_field; default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) => {
        lazy val safeId = value.asLong.getOrElse(-1L)
        field match {
          case "id"         => algebra.getAirlineRoutesByAirlineId(safeId)
          case "country_id" => algebra.getAirlineRoutesByAirline(field, safeId)
          case _            => algebra.getAirlineRoutesByAirline(field, value)
        }
      }.flatMap(toResponse(_))

    // GET /airline-routes/airplane/{value}?field={airplane_field; default: id}
    case GET -> Root / "airplane" / value :? FieldMatcherIdDefault(field) => {
        lazy val safeId = value.asLong.getOrElse(-1L)
        field match {
          case "id"              => algebra.getAirlineRoutesByAirplaneId(safeId)
          case "manufacturer_id" => algebra.getAirlineRoutesByAirplane(field, safeId)
          case _                 => algebra.getAirlineRoutesByAirplane(field, value)
        }
      }.flatMap(toResponse(_))

    // GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound
    case GET -> Root / "airport" / value :? FieldMatcherIdDefault(field) +&
          InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) => {
        lazy val safeId = value.asLong.getOrElse(-1L)
        val direction = (inbound, outbound) match {
          case (true, false) => Some(true)
          case (false, true) => Some(false)
          case _             => None
        }

        field match {
          case "id" | "city_id" => algebra.getAirlineRoutesByAirport(field, safeId, direction)
          case _                => algebra.getAirlineRoutesByAirport(field, value, direction)
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
