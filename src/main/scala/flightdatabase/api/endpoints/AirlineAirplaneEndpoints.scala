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

    // GET /airline-airplanes?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[AirlineAirplane](sortAndLimit) {
        processReturnOnly2[AirlineAirplane](_, returnOnly)(algebra.getAirlineAirplanes)
      }

    // GET /airline-airplanes/{id}
    case GET -> Root / LongVar(id) =>
      algebra.getAirlineAirplane(id).flatMap(_.toResponse)

    // GET /airline-airplanes/airline/{airline_id}/airplane/{airplane_id}
    case GET -> Root / "airline" / LongVar(airlineId) / "airplane" / LongVar(airplaneId) =>
      algebra.getAirlineAirplane(airlineId, airplaneId).flatMap(_.toResponse)

    // GET /airline-airplanes/filter?field={airline_airplane_field}&operator={operator; default: eq}&value={value}&sort-by={airline_airplane_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[AirlineAirplane](sortAndLimit) {
        processFilter2[AirlineAirplane, AirlineAirplane](field, operator, values, _)(
          algebra.getAirlineAirplanesBy
        )
      }

    // GET /airline-airplanes/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}&sort-by={airline_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airline](sortAndLimit) {
        processFilter2[Airline, AirlineAirplane](field, operator, values, _)(
          algebra.getAirlineAirplanesByAirline
        )
      }

    // GET /airline-airplanes/airplane/filter?field={airplane_field}&operator={operator; default: eq}&value={value}&sort-by={airplane_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airplane" / value :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airplane](sortAndLimit) {
        processFilter2[Airplane, AirlineAirplane](field, operator, values, _)(
          algebra.getAirlineAirplanesByAirplane
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
