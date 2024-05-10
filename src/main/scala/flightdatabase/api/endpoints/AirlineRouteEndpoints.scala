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
        algebra.getAirlineRoutesOnlyRoutes.flatMap(_.toResponse)
      } else {
        algebra.getAirlineRoutes.flatMap(_.toResponse)
      }

    // GET /airline-routes/filter?field={airline-route-field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[AirlineRoute, AirlineRoute](field, operator, values)(
        stringF = algebra.getAirlineRoutesBy,
        intF = algebra.getAirlineRoutesBy,
        longF = algebra.getAirlineRoutesBy,
        boolF = algebra.getAirlineRoutesBy,
        bigDecimalF = algebra.getAirlineRoutesBy
      )

    // GET /airline-routes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineRoute)

    // GET /airline-routes/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airline, AirlineRoute](field, operator, values)(
        stringF = algebra.getAirlineRoutesByAirline,
        intF = algebra.getAirlineRoutesByAirline,
        longF = algebra.getAirlineRoutesByAirline,
        boolF = algebra.getAirlineRoutesByAirline,
        bigDecimalF = algebra.getAirlineRoutesByAirline
      )

    // GET /airline-routes/airplane/filter?field={airplane_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airplane" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airplane, AirlineRoute](field, operator, values)(
        stringF = algebra.getAirlineRoutesByAirplane,
        intF = algebra.getAirlineRoutesByAirplane,
        longF = algebra.getAirlineRoutesByAirplane,
        boolF = algebra.getAirlineRoutesByAirplane,
        bigDecimalF = algebra.getAirlineRoutesByAirplane
      )

    // GET /airline-routes/airport/filter?field={airport_field}&operator={operator; default: eq}&value={value}&inbound&outbound
    case GET -> Root / "airport" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) +&
            InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) =>
      val direction = (inbound, outbound) match {
        case (true, false) => Some(true)
        case (false, true) => Some(false)
        case _             => None
      }

      processFilter[Airport, AirlineRoute](field, operator, values)(
        stringF = algebra.getAirlineRoutesByAirport(_, _, _, direction),
        intF = algebra.getAirlineRoutesByAirport(_, _, _, direction),
        longF = algebra.getAirlineRoutesByAirport(_, _, _, direction),
        boolF = algebra.getAirlineRoutesByAirport(_, _, _, direction),
        bigDecimalF = algebra.getAirlineRoutesByAirport(_, _, _, direction)
      )

    // POST /airline-routes
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirlineRoute).flatMap(_.toResponse)

    // PUT /airline-routes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirlineRouteCreate, Long](req) { airlineRoute =>
          if (airlineRoute.id.exists(_ != i)) {
            InconsistentIds(i, airlineRoute.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineRoute(AirlineRoute.fromCreate(i, airlineRoute))
          }
        }
      }

    // PATCH /airline-routes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateAirlineRoute(i, _)))

    // DELETE /airline-routes/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirlineRoute)
  }
}

object AirlineRouteEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineRouteAlgebra[F]): Endpoints[F] =
    new AirlineRouteEndpoints(prefix, algebra)
}
