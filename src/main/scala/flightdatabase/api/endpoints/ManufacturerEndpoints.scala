package flightdatabase.api.endpoints

import cats.Applicative
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

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /manufacturers/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getManufacturer(id).flatMap(toResponse(_)))

    // GET /manufacturers/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getManufacturers("name", name).flatMap(toResponse(_))

    // GET /manufacturers/city/{city_name} OR
    // GET /manufacturers/city/{city_id}
    case GET -> Root / "city" / city =>
      city.asLong.fold[F[Response[F]]] {
        // Treat city as name
        algebra.getManufacturersByCity(city).flatMap(toResponse(_))
      }(algebra.getManufacturers("city_id", _).flatMap(toResponse(_)))

    // GET /manufacturers/country/{country_name}
    case GET -> Root / "country" / country =>
      algebra.getManufacturersByCountry(country).flatMap(toResponse(_))

    // POST /manufacturers
    case req @ POST -> Root =>
      req
        .attemptAs[ManufacturerCreate]
        .foldF[ApiResult[Long]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
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
          .foldF[ApiResult[Manufacturer]](
            _ => Applicative[F].pure(Left(EntryInvalidFormat)),
            m =>
              if (id != m.id) {
                Applicative[F].pure(Left(InconsistentIds(id, m.id)))
              } else {
                algebra.updateManufacturer(m)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /manufacturers/{id}
    case req @ PATCH -> Root / LongVar(id) =>
      req
        .attemptAs[ManufacturerPatch]
        .foldF[ApiResult[Manufacturer]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
          algebra.partiallyUpdateManufacturer(id, _)
        )
        .flatMap(toResponse(_))

    // DELETE /manufacturers/{id}
    case DELETE -> Root / LongVar(id) =>
      algebra.removeManufacturer(id).flatMap(toResponse(_))
  }
}

object ManufacturerEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: ManufacturerAlgebra[F]
  ): Endpoints[F] = new ManufacturerEndpoints(prefix, algebra)
}
