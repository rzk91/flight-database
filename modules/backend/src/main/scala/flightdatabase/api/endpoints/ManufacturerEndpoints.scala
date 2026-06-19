package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase._
import flightdatabase.city.City
import flightdatabase.country.Country
import flightdatabase.extensions.string._
import flightdatabase.manufacturer.Manufacturer
import flightdatabase.manufacturer.ManufacturerAlgebra
import flightdatabase.manufacturer.ManufacturerCreate
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

    // GET /manufacturers?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Manufacturer](sortAndLimit) {
        processReturnOnly[Manufacturer](_, returnOnly)(algebra.getManufacturers)
      }

    // GET /manufacturers/filter?field={manufacturer_field}&operator={operator; default: eq}&value={value}&sort-by={manufacturer_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Manufacturer](sortAndLimit) {
        processFilter[Manufacturer, Manufacturer](field, operator, values, _)(
          algebra.getManufacturersBy
        )
      }

    // GET /manufacturers/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getManufacturer)

    // GET /manufacturers/city/filter?field={city_field}&operator={operator; default: eq}&value={value}&sort-by={city_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[City](sortAndLimit) {
        processFilter[City, Manufacturer](field, operator, values, _)(
          algebra.getManufacturersByCity
        )
      }

    // GET /manufacturers/country/filter?field={country_field}&operator={operator; default: eq}&value={value}&sort-by={country_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processFilter[Country, Manufacturer](field, operator, values, _)(
          algebra.getManufacturersByCountry
        )
      }

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
