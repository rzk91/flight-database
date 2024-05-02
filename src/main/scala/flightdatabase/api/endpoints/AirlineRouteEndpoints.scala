package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteAlgebra
import flightdatabase.domain.airline_route.AirlineRouteCreate
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airport.Airport
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
      withFieldValidation[AirlineRoute](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getAirlineRoute)
          case str if str.endsWith("id") =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlineRoutes(field, _))
          case _ => algebra.getAirlineRoutes(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airline-routes/airline/{value}?field={airline_field; default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Airline](field) {
        field match {
          case "id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlineRoutesByAirlineId)
          case "country_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(
              algebra.getAirlineRoutesByAirline(field, _)
            )
          case _ => algebra.getAirlineRoutesByAirline(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airline-routes/airplane/{value}?field={airplane_field; default: id}
    case GET -> Root / "airplane" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Airplane](field) {
        field match {
          case "id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlineRoutesByAirplaneId)
          case "manufacturer_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(
              algebra.getAirlineRoutesByAirplane(field, _)
            )
          case _ => algebra.getAirlineRoutesByAirplane(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound
    case GET -> Root / "airport" / value :? FieldMatcherIdDefault(field) +&
          InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) =>
      val direction = (inbound, outbound) match {
        case (true, false) => Some(true)
        case (false, true) => Some(false)
        case _             => None
      }

      withFieldValidation[Airport](field) {
        field match {
          case "id" | "city_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(
              algebra.getAirlineRoutesByAirport(field, _, direction)
            )
          case _ =>
            algebra.getAirlineRoutesByAirport(field, value, direction).flatMap(toResponse(_))
        }
      }

    // POST /airline-routes
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirlineRoute).flatMap(toResponse(_))

    // PUT /airline-routes/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[AirlineRouteCreate, Long](req) { airlineRoute =>
          if (airlineRoute.id.exists(_ != i)) {
            InconsistentIds(i, airlineRoute.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineRoute(AirlineRoute.fromCreate(i, airlineRoute))
          }
        }
      }

    // PATCH /airline-routes/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirlineRoute(i, _)))

    // DELETE /airline-routes/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirlineRoute)
  }
}

object AirlineRouteEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineRouteAlgebra[F]): Endpoints[F] =
    new AirlineRouteEndpoints(prefix, algebra)
}
