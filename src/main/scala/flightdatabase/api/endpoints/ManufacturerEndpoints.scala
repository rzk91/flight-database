package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
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
        algebra.getManufacturersOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getManufacturers.flatMap(toResponse(_))
      }

    // GET /manufacturers/{value}=field={manufacturer_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      field match {
        case "id" => idToResponse(value)(algebra.getManufacturer)
        case "base_city_id" =>
          idToResponse(value, EntryHasInvalidForeignKey)(algebra.getManufacturers(field, _))
        case _ => algebra.getManufacturers(field, value).flatMap(toResponse(_))
      }

    // GET /manufacturers/city/{value}?field={city_field; default=id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        idToResponse(value, EntryHasInvalidForeignKey)(algebra.getManufacturersByCity(field, _))
      } else {
        algebra.getManufacturersByCity(field, value).flatMap(toResponse(_))
      }

    // GET /manufacturers/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        idToResponse(value, EntryHasInvalidForeignKey)(algebra.getManufacturersByCountry(field, _))
      } else {
        algebra.getManufacturersByCountry(field, value).flatMap(toResponse(_))
      }

    // POST /manufacturers
    case req @ POST -> Root =>
      processRequest(req)(algebra.createManufacturer).flatMap(toResponse(_))

    // PUT /manufacturers/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Manufacturer, Long](req) { manufacturer =>
          if (i != manufacturer.id) {
            InconsistentIds(i, manufacturer.id).elevate[F, Long]
          } else {
            algebra.updateManufacturer(manufacturer)
          }
        }
      }

    // PATCH /manufacturers/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateManufacturer(i, _)))

    // DELETE /manufacturers/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeManufacturer)
  }
}

object ManufacturerEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: ManufacturerAlgebra[F]
  ): Endpoints[F] = new ManufacturerEndpoints(prefix, algebra)
}
