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

    // GET /airplanes/{value}?field={airplane_field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getAirplane)
      } else {
        implicitly[TableBase[Airplane]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getAirplanes(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getAirplanes(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getAirplanes(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getAirplanes(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getAirplanes(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // GET /airplanes/manufacturer/{value}?field={manufacturer_field; default: id}
    case GET -> Root / "manufacturer" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Manufacturer]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirplanesByManufacturer(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getAirplanesByManufacturer(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getAirplanesByManufacturer(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirplanesByManufacturer(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirplanesByManufacturer(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /airplanes
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirplane).flatMap(_.toResponse)

    // PUT /airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[AirplaneCreate, Long](req) { airplane =>
          if (airplane.id.exists(_ != i)) {
            InconsistentIds(i, airplane.id.get).elevate[F, Long]
          } else {
            algebra.updateAirplane(Airplane.fromCreate(i, airplane))
          }
        }
      }

    // PATCH /airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateAirplane(i, _)))

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
