package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.api.toResponse
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.airline.AirlinePatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirlineAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airlines/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airlines?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirlinesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getAirlines.flatMap(toResponse(_))
      }

    // GET /airlines/{value}?field={airline_field, default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id"         => algebra.getAirline(safeId).flatMap(toResponse(_))
        case "country_id" => algebra.getAirlines("country_id", safeId).flatMap(toResponse(_))
        case _            => algebra.getAirlines(field, value).flatMap(toResponse(_))
      }

    // GET /airlines/country/{value}?field={country_field, default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      if (field.endsWith("id")) {
        algebra.getAirlinesByCountry(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getAirlinesByCountry(field, value).flatMap(toResponse(_))
      }

    // POST /airlines
    case req @ POST -> Root =>
      req
        .attemptAs[AirlineCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirline
        )
        .flatMap(toResponse(_))

    // PUT /airlines/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Airline]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            airline =>
              if (id != airline.id) {
                InconsistentIds(id, airline.id).elevate[F, Long]
              } else {
                algebra.updateAirline(airline)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airlines/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirlinePatch]
          .foldF[ApiResult[Airline]](
            _ => EntryInvalidFormat.elevate[F, Airline],
            algebra.partiallyUpdateAirline(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airlines/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirline(id).flatMap(toResponse(_)))
  }
}

object AirlineEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAlgebra[F]): Endpoints[F] =
    new AirlineEndpoints[F](prefix, algebra)
}
