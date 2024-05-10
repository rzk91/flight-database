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

    // GET /manufacturers/filter?field={manufacturer_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Manufacturer, Manufacturer](field, operator, values)(
        stringF = algebra.getManufacturersBy,
        intF = algebra.getManufacturersBy,
        longF = algebra.getManufacturersBy,
        boolF = algebra.getManufacturersBy,
        bigDecimalF = algebra.getManufacturersBy
      )

    // GET /manufacturers/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getManufacturer)

    // GET /manufacturers/city/filter?field={city_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[City, Manufacturer](field, operator, values)(
        stringF = algebra.getManufacturersByCity,
        intF = algebra.getManufacturersByCity,
        longF = algebra.getManufacturersByCity,
        boolF = algebra.getManufacturersByCity,
        bigDecimalF = algebra.getManufacturersByCity
      )

    // GET /manufacturers/country/filter?field={country_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Country, Manufacturer](field, operator, values)(
        stringF = algebra.getManufacturersByCountry,
        intF = algebra.getManufacturersByCountry,
        longF = algebra.getManufacturersByCountry,
        boolF = algebra.getManufacturersByCountry,
        bigDecimalF = algebra.getManufacturersByCountry
      )

    // POST /manufacturers
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createManufacturer).flatMap(_.toResponse)

    // PUT /manufacturers/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[ManufacturerCreate, Long](req) { manufacturer =>
          if (manufacturer.id.exists(_ != i)) {
            InconsistentIds(i, manufacturer.id.get).elevate[F, Long]
          } else {
            algebra.updateManufacturer(Manufacturer.fromCreate(i, manufacturer))
          }
        }
      }

    // PATCH /manufacturers/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateManufacturer(i, _)))

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
