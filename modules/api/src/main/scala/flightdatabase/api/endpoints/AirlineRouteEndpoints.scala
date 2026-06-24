package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase._
import flightdatabase.airline.Airline
import flightdatabase.airline_route.AirlineRoute
import flightdatabase.airline_route.AirlineRouteAlgebra
import flightdatabase.airline_route.AirlineRouteCreate
import flightdatabase.airplane.Airplane
import flightdatabase.airport.Airport
import flightdatabase.syntax.string._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineRouteEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineRouteAlgebra[F]
) extends Endpoints[F](prefix) {

  private object InboundFlagMatcher extends FlagQueryParamMatcher("inbound")
  private object OutboundFlagMatcher extends FlagQueryParamMatcher("outbound")

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-routes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineRouteExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-routes?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[AirlineRoute](sortAndLimit) {
        processReturnOnly[AirlineRoute](_, returnOnly)(algebra.getAirlineRoutes)
      }

    // GET /airline-routes/filter?field={airline-route-field}&operator={operator; default: eq}&value={value}&sort-by={airline-route-field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[AirlineRoute](sortAndLimit) {
        processFilter[AirlineRoute, AirlineRoute](field, operator, values, _)(
          algebra.getAirlineRoutesBy
        )
      }

    // GET /airline-routes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineRoute)

    // GET /airline-routes/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}&sort-by={airline_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airline](sortAndLimit) {
        processFilter[Airline, AirlineRoute](field, operator, values, _)(
          algebra.getAirlineRoutesByAirline
        )
      }

    // GET /airline-routes/airplane/filter?field={airplane_field}&operator={operator; default: eq}&value={value}&sort-by={airplane_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airplane" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airplane](sortAndLimit) {
        processFilter[Airplane, AirlineRoute](field, operator, values, _)(
          algebra.getAirlineRoutesByAirplane
        )
      }

    // GET /airline-routes/airport/filter?field={airport_field}&operator={operator; default: eq}&value={value}&inbound&outbound&sort-by={airport_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airport" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) +&
            InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) +&
            SortAndLimit(sortAndLimit) =>
      val direction = (inbound, outbound) match {
        case (true, false) => Some(true)
        case (false, true) => Some(false)
        case _             => None
      }

      withSortAndLimitValidation[Airport](sortAndLimit) {
        processFilter[Airport, AirlineRoute](field, operator, values, _)(
          algebra.getAirlineRoutesByAirport(direction)
        )
      }

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
