package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.api.toResponse
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
import flightdatabase.domain.manufacturer.ManufacturerCreate
import flightdatabase.domain.manufacturer.ManufacturerPatch
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
        algebra.getManufacturersOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getManufacturers.flatMap(toResponse(_))
      }

    // GET /manufacturers/{value}=field={manufacturer_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id"           => algebra.getManufacturer(safeId).flatMap(toResponse(_))
        case "base_city_id" => algebra.getManufacturers(field, safeId).flatMap(toResponse(_))
        case _              => algebra.getManufacturers(field, value).flatMap(toResponse(_))
      }

    // GET /manufacturers/city/{value}?field={city_field; default=id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        val safeId = value.asLong.getOrElse(-1L)
        algebra.getManufacturersByCity(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getManufacturersByCity(field, value).flatMap(toResponse(_))
      }

    // GET /manufacturers/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        val safeId = value.asLong.getOrElse(-1L)
        algebra.getManufacturersByCountry(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getManufacturersByCountry(field, value).flatMap(toResponse(_))
      }

    // POST /manufacturers
    case req @ POST -> Root =>
      req
        .attemptAs[ManufacturerCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createManufacturer
        )
        .flatMap(toResponse(_))

    // PUT /manufacturers/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Manufacturer]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            m =>
              if (id != m.id) {
                InconsistentIds(id, m.id).elevate[F, Long]
              } else {
                algebra.updateManufacturer(m)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /manufacturers/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[ManufacturerPatch]
          .foldF[ApiResult[Manufacturer]](
            _ => EntryInvalidFormat.elevate[F, Manufacturer],
            algebra.partiallyUpdateManufacturer(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /manufacturers/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeManufacturer(id).flatMap(toResponse(_)))
  }
}

object ManufacturerEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: ManufacturerAlgebra[F]
  ): Endpoints[F] = new ManufacturerEndpoints(prefix, algebra)
}
