package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate
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

    // GET /airline-airplanes/filter?field={airline_airplane_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineAirplane](field, op) {
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanes(field, _, op)
          )
      }

    // GET /airline-airplanes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / airlineId / "airplane" / airplaneId =>
      (airlineId.asLong, airplaneId.asLong).tupled.toResponse(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/filter?field={airline_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineAirplane](field, op) {
        case Some(StringType) if StringType.operators(op) =>
          values.asStringToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirline(field, _, op)
          )
        case Some(IntType) if IntType.operators(op) =>
          values.asIntToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirline(field, _, op)
          )
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirline(field, _, op)
          )
        case Some(BooleanType) if BooleanType.operators(op) =>
          values.asBooleanToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirline(field, _, op)
          )
        case Some(BigDecimalType) if BigDecimalType.operators(op) =>
          values.asBigDecimalToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirline(field, _, op)
          )
      }

    // GET /airline-airplanes/airplane/filter?field={airplane_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airplane" / value :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineAirplane](field, op) {
        case Some(StringType) if StringType.operators(op) =>
          values.asStringToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirplane(field, _, op)
          )
        case Some(IntType) if IntType.operators(op) =>
          values.asIntToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirplane(field, _, op)
          )
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirplane(field, _, op)
          )
        case Some(BooleanType) if BooleanType.operators(op) =>
          values.asBooleanToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirplane(field, _, op)
          )
        case Some(BigDecimalType) if BigDecimalType.operators(op) =>
          values.asBigDecimalToResponse[F, List[AirlineAirplane]](field, op)(
            algebra.getAirlineAirplanesByAirplane(field, _, op)
          )
      }

    // POST /airline-airplanes
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirlineAirplane).flatMap(_.toResponse)

    // PUT /airline-airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirlineAirplaneCreate, Long](req) { aa =>
          if (aa.id.exists(_ != i)) {
            InconsistentIds(i, aa.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineAirplane(AirlineAirplane.fromCreate(i, aa))
          }
        }
      }

    // PATCH /airline-airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i =>
        processRequestBody(req)(algebra.partiallyUpdateAirlineAirplane(i, _))
      )

    // DELETE /airline-airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirlineAirplane)
  }
}

object AirlineAirplaneEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAirplaneAlgebra[F]): Endpoints[F] =
    new AirlineAirplaneEndpoints(prefix, algebra)
}
