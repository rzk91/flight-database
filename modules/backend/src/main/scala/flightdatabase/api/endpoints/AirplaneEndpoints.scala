package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase._
import flightdatabase.airplane.Airplane
import flightdatabase.airplane.AirplaneAlgebra
import flightdatabase.airplane.AirplaneCreate
import flightdatabase.extensions.string._
import flightdatabase.manufacturer.Manufacturer
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirplaneAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airplanes/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirplaneExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airplanes?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Airplane](sortAndLimit) {
        processReturnOnly[Airplane](_, returnOnly)(algebra.getAirplanes)
      }

    // GET /airplanes/filter?field={airplane_field}&operator={operator; default: eq}&value={value}&sort-by={airplane_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airplane](sortAndLimit) {
        processFilter[Airplane, Airplane](field, operator, values, _)(algebra.getAirplanesBy)
      }

    // GET /airplanes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirplane)

    // GET /airplanes/manufacturer/filter?field={manufacturer_field}&operator={operator; default: eq}&value={value}&sort-by={manufacturer_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "manufacturer" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Manufacturer](sortAndLimit) {
        processFilter[Manufacturer, Airplane](field, operator, values, _)(
          algebra.getAirplanesByManufacturer
        )
      }

    // POST /airplanes
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirplane).flatMap(_.toResponse)

    // PUT /airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirplaneCreate, Long](req) { airplane =>
          if (airplane.id.exists(_ != i)) {
            InconsistentIds(i, airplane.id.get).elevate[F, Long]
          } else {
            algebra.updateAirplane(Airplane.fromCreate(i, airplane))
          }
        }
      }

    // PATCH /airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateAirplane(i, _)))

    // DELETE /airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirplane)
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  ): Endpoints[F] =
    new AirplaneEndpoints(prefix, algebra)
}
