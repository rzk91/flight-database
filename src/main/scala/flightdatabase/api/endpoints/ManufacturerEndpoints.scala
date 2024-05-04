package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.domain._
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
import flightdatabase.domain.manufacturer.ManufacturerCreate
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class ManufacturerEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: ManufacturerAlgebra[F]
) extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /manufacturers/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesManufacturerExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /manufacturers?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getManufacturersOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getManufacturers.flatMap(_.toResponse)
      }

    // GET /manufacturers/{value}=field={manufacturer_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getManufacturer)
      } else {
        implicitly[TableBase[Manufacturer]].fieldTypeMap.get(field) match {
          case Some(StringType)  => algebra.getManufacturers(field, value).flatMap(_.toResponse)
          case Some(IntType)     => value.asInt.toResponse(algebra.getManufacturers(field, _))
          case Some(LongType)    => value.asLong.toResponse(algebra.getManufacturers(field, _))
          case Some(BooleanType) => value.asBoolean.toResponse(algebra.getManufacturers(field, _))
          case Some(BigDecimalType) =>
            value.asBigDecimal.toResponse(algebra.getManufacturers(field, _))
          case None => BadRequest(InvalidField(field).error)
        }
      }

    // GET /manufacturers/city/{value}?field={city_field; default=id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[City]].fieldTypeMap.get(field) match {
        case Some(StringType) => algebra.getManufacturersByCity(field, value).flatMap(_.toResponse)
        case Some(IntType)    => value.asInt.toResponse(algebra.getManufacturersByCity(field, _))
        case Some(LongType)   => value.asLong.toResponse(algebra.getManufacturersByCity(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getManufacturersByCity(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getManufacturersByCity(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /manufacturers/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Country]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getManufacturersByCountry(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getManufacturersByCountry(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getManufacturersByCountry(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getManufacturersByCountry(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getManufacturersByCountry(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /manufacturers
    case req @ POST -> Root =>
      processRequest(req)(algebra.createManufacturer).flatMap(_.toResponse)

    // PUT /manufacturers/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[ManufacturerCreate, Long](req) { manufacturer =>
          if (manufacturer.id.exists(_ != i)) {
            InconsistentIds(i, manufacturer.id.get).elevate[F, Long]
          } else {
            algebra.updateManufacturer(Manufacturer.fromCreate(i, manufacturer))
          }
        }
      }

    // PATCH /manufacturers/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateManufacturer(i, _)))

    // DELETE /manufacturers/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeManufacturer)
  }
}

object ManufacturerEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: ManufacturerAlgebra[F]
  ): Endpoints[F] = new ManufacturerEndpoints(prefix, algebra)
}
