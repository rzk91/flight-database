package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.utils.implicits.enrichString
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

    // GET /airplanes?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirplanesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getAirplanes.flatMap(_.toResponse)
      }

    // GET /airplanes/filter?field={airplane_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airplane, Airplane](field, operator, values)(
        stringF = algebra.getAirplanesBy,
        intF = algebra.getAirplanesBy,
        longF = algebra.getAirplanesBy,
        boolF = algebra.getAirplanesBy,
        bigDecimalF = algebra.getAirplanesBy
      )

    // GET /airplanes/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirplane)

    // GET /airplanes/manufacturer/filter?field={manufacturer_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "manufacturer" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Manufacturer, Airplane](field, operator, values)(
        stringF = algebra.getAirplanesByManufacturer,
        intF = algebra.getAirplanesByManufacturer,
        longF = algebra.getAirplanesByManufacturer,
        boolF = algebra.getAirplanesByManufacturer,
        bigDecimalF = algebra.getAirplanesByManufacturer
      )

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
