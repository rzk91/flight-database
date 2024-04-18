package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.airplane.AirplanePatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirplaneAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /airplanes/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getAirplane(id).flatMap(toResponse(_)))

    // GET /airplanes/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getAirplanes("name", name).flatMap(toResponse(_))

    // GET /airplanes/manufacturer/{manufacturer_name} OR
    // GET /airplanes/manufacturer/{manufacturer_id}
    case GET -> Root / "manufacturer" / manufacturer =>
      manufacturer.asLong.fold[F[Response[F]]] {
        // Treat manufacturer as name
        algebra.getAirplanesByManufacturer(manufacturer).flatMap(toResponse(_))
      }(algebra.getAirplanes("manufacturer_id", _).flatMap(toResponse(_)))

    // POST /airplanes
    case req @ POST -> Root =>
      req
        .attemptAs[AirplaneCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirplane
        )
        .flatMap(toResponse(_))

    // PUT /airplanes/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Airplane]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            airplane =>
              if (id != airplane.id) {
                InconsistentIds(id, airplane.id).elevate[F, Long]
              } else {
                algebra.updateAirplane(airplane)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airplanes/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirplanePatch]
          .foldF[ApiResult[Airplane]](
            _ => EntryInvalidFormat.elevate[F, Airplane],
            algebra.partiallyUpdateAirplane(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airplanes/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirplane(id).flatMap(toResponse(_)))
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  ): Endpoints[F] =
    new AirplaneEndpoints(prefix, algebra)
}
