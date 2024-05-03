package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits.catsSyntaxTuple2Semigroupal
import cats.syntax.flatMap._
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate
import flightdatabase.domain.airplane.Airplane
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineAirplaneEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineAirplaneAlgebra[F]
) extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-airplanes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineAirplaneExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-airplanes
    case GET -> Root =>
      algebra.getAirlineAirplanes.flatMap(_.toResponse)

    // GET /airline-airplanes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / airlineId / "airplane" / airplaneId =>
      (airlineId.asLong, airplaneId.asLong).tupled.toResponse(algebra.getAirlineAirplane)

    // TODO: Refactor to use `processRequestByField` or the like, if possible
    //  For now, I can't think of a better way to solve this issue

    // GET /airline-airplanes/airline/{value}?field={airline_field; default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Airline]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineAirplanesByAirline(field, value).flatMap(_.toResponse)
        case Some(IntType) => value.asInt.toResponse(algebra.getAirlineAirplanesByAirline(field, _))
        case Some(LongType) =>
          value.asLong.toResponse(algebra.getAirlineAirplanesByAirline(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineAirplanesByAirline(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineAirplanesByAirline(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /airline-airplanes/airplane/{value}?field={airplane_field; default: id}
    case GET -> Root / "airplane" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Airplane]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineAirplanesByAirplane(field, value).flatMap(_.toResponse)
        case Some(IntType) =>
          value.asInt.toResponse(algebra.getAirlineAirplanesByAirplane(field, _))
        case Some(LongType) =>
          value.asLong.toResponse(algebra.getAirlineAirplanesByAirplane(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineAirplanesByAirplane(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineAirplanesByAirplane(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /airline-airplanes
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirlineAirplane).flatMap(_.toResponse)

    // PUT /airline-airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[AirlineAirplaneCreate, Long](req) { aa =>
          if (aa.id.exists(_ != i)) {
            InconsistentIds(i, aa.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineAirplane(AirlineAirplane.fromCreate(i, aa))
          }
        }
      }

    // PATCH /airline-airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateAirlineAirplane(i, _)))

    // DELETE /airline-airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirlineAirplane)
  }
}

object AirlineAirplaneEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAirplaneAlgebra[F]): Endpoints[F] =
    new AirlineAirplaneEndpoints(prefix, algebra)
}
