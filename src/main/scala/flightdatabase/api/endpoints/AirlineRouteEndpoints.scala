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

    // GET /airline-routes/{value}?field={airline-route-field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getAirlineRoute)
      } else {
        implicitly[TableBase[AirlineRoute]].fieldTypeMap.get(field) match {
          case Some(StringType)  => algebra.getAirlineRoutes(field, value).flatMap(_.toResponse)
          case Some(IntType)     => value.asInt.toResponse(algebra.getAirlineRoutes(field, _))
          case Some(LongType)    => value.asLong.toResponse(algebra.getAirlineRoutes(field, _))
          case Some(BooleanType) => value.asBoolean.toResponse(algebra.getAirlineRoutes(field, _))
          case Some(BigDecimalType) =>
            value.asBigDecimal.toResponse(algebra.getAirlineRoutes(field, _))
          case None => BadRequest(InvalidField(field).error)
        }
      }

    // GET /airline-routes/airline/{value}?field={airline_field; default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Airline]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineRoutesByAirline(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getAirlineRoutesByAirline(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getAirlineRoutesByAirline(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineRoutesByAirline(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineRoutesByAirline(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /airline-routes/airplane/{value}?field={airplane_field; default: id}
    case GET -> Root / "airplane" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Airplane]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineRoutesByAirplane(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getAirlineRoutesByAirplane(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getAirlineRoutesByAirplane(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineRoutesByAirplane(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineRoutesByAirplane(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /airline-routes/airport/{value}?field={airport_field; default: id}&inbound&outbound
    case GET -> Root / "airport" / value :? FieldMatcherIdDefault(field) +&
          InboundFlagMatcher(inbound) +& OutboundFlagMatcher(outbound) =>
      val direction = (inbound, outbound) match {
        case (true, false) => Some(true)
        case (false, true) => Some(false)
        case _             => None
      }

      implicitly[TableBase[Airport]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineRoutesByAirport(field, value, direction).flatMap(_.toResponse)
        case Some(IntType) =>
          value.asInt.toResponse(algebra.getAirlineRoutesByAirport(field, _, direction))
        case Some(LongType) =>
          value.asLong.toResponse(algebra.getAirlineRoutesByAirport(field, _, direction))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineRoutesByAirport(field, _, direction))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineRoutesByAirport(field, _, direction))
        case None => BadRequest(InvalidField(field).error)
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
