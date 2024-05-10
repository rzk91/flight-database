package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
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

    // All methods are the same but for the type of object which is only determined based on `field`
    // I can not seem to figure out how to make this more DRY
    // GET /airline-airplanes/filter?field={airline_airplane_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[AirlineAirplane, AirlineAirplane](field, operator, values)(
        stringF = algebra.getAirlineAirplanesBy,
        intF = algebra.getAirlineAirplanesBy,
        longF = algebra.getAirlineAirplanesBy,
        boolF = algebra.getAirlineAirplanesBy,
        bigDecimalF = algebra.getAirlineAirplanesBy
      )

    // GET /airline-airplanes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / airlineId / "airplane" / airplaneId =>
      (airlineId.asLong, airplaneId.asLong).tupled.toResponse(algebra.getAirlineAirplane)

    // GET /airline-airplanes/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airline, AirlineAirplane](field, operator, values)(
        stringF = algebra.getAirlineAirplanesByAirline,
        intF = algebra.getAirlineAirplanesByAirline,
        longF = algebra.getAirlineAirplanesByAirline,
        boolF = algebra.getAirlineAirplanesByAirline,
        bigDecimalF = algebra.getAirlineAirplanesByAirline
      )

    // GET /airline-airplanes/airplane/filter?field={airplane_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airplane" / value :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airplane, AirlineAirplane](field, operator, values)(
        stringF = algebra.getAirlineAirplanesByAirplane,
        intF = algebra.getAirlineAirplanesByAirplane,
        longF = algebra.getAirlineAirplanesByAirplane,
        boolF = algebra.getAirlineAirplanesByAirplane,
        bigDecimalF = algebra.getAirlineAirplanesByAirplane
      )

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
