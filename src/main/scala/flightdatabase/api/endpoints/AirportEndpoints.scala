package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.api.toResponse
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.airport.AirportPatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirportEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirportAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airports/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirportExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airports?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirportsOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getAirports.flatMap(toResponse(_))
      }

    // GET /airports/{valid}?field={airport_field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      lazy val safeId = value.asLong.getOrElse(-1L)
      field match {
        case "id"      => algebra.getAirport(safeId).flatMap(toResponse(_))
        case "city_id" => algebra.getAirports(field, safeId).flatMap(toResponse(_))
        case _         => algebra.getAirports(field, value).flatMap(toResponse(_))
      }

    // GET /airports/city/{value}?field={city_field; default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        val safeId = value.asLong.getOrElse(-1L)
        algebra.getAirportsByCity(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getAirportsByCity(field, value).flatMap(toResponse(_))
      }

    // GET /airports/country/{value}?field={country_field; default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      if (field.endsWith("id")) {
        val safeId = value.asLong.getOrElse(-1L)
        algebra.getAirportsByCountry(field, safeId).flatMap(toResponse(_))
      } else {
        algebra.getAirportsByCountry(field, value).flatMap(toResponse(_))
      }

    // POST /airports
    case req @ POST -> Root =>
      req
        .attemptAs[AirportCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createAirport
        )
        .flatMap(toResponse(_))

    // PUT /airports/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Airport]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            airport =>
              if (id != airport.id) {
                InconsistentIds(id, airport.id).elevate[F, Long]
              } else {
                algebra.updateAirport(airport)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /airports/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[AirportPatch]
          .foldF[ApiResult[Airport]](
            _ => EntryInvalidFormat.elevate[F, Airport],
            algebra.partiallyUpdateAirport(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /airports/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeAirport(id).flatMap(toResponse(_)))
  }
}

object AirportEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirportAlgebra[F]): Endpoints[F] =
    new AirportEndpoints(prefix, algebra)
}
