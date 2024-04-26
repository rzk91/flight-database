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

  private object FieldMatcher extends QueryParamDecoderMatcherWithDefault[String]("field", "name")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /airlines/{value}?field={id/name/iata/icao/call_sign, default: id/name}
    case GET -> Root / value :? FieldMatcher(field) =>
      value.asLong match {
        case Some(id) => algebra.getAirline(id).flatMap(toResponse(_))
        case None     => algebra.getAirlines(field, value).flatMap(toResponse(_))
      }

    // GET /airlines/country/{value}?field={id/name/iso2/iso3/country_code/domain_name, default: name}
    case GET -> Root / "country" / value :? FieldMatcher(field) =>
      (value.asLong match {
        case Some(countryId) => algebra.getAirlinesByCountry(field, countryId)
        case None            => algebra.getAirlinesByCountry(field, value)
      }).flatMap(toResponse(_))

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
