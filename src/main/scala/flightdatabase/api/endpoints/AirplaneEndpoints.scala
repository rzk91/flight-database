package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.manufacturer.Manufacturer
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
        algebra.getAirplanesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getAirplanes.flatMap(toResponse(_))
      }

    // GET /airplanes/{value}?field={airplane_field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Airplane](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getAirplane)
          case "manufacturer_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirplanes(field, _))
          case _ => algebra.getAirplanes(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airplanes/manufacturer/{value}?field={manufacturer_field; default: id}
    case GET -> Root / "manufacturer" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Manufacturer](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(
            algebra.getAirplanesByManufacturer(field, _)
          )
        } else {
          algebra.getAirplanesByManufacturer(field, value).flatMap(toResponse(_))
        }
      }

    // POST /airplanes
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirplane).flatMap(toResponse(_))

    // PUT /airplanes/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Airplane, Long](req) { airplane =>
          if (i != airplane.id) {
            InconsistentIds(i, airplane.id).elevate[F, Long]
          } else {
            algebra.updateAirplane(airplane)
          }
        }
      }

    // PATCH /airplanes/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirplane(i, _)))

    // DELETE /airplanes/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirplane)
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  ): Endpoints[F] =
    new AirplaneEndpoints(prefix, algebra)
}
