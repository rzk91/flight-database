package flightdatabase.api.endpoints

import cats.Applicative
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

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
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

    // GET /airports/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getAirport(id).flatMap(toResponse(_)))

    // GET /airports/iata/{iata}
    case GET -> Root / "iata" / iata =>
      algebra.getAirportsBy("iata", iata).flatMap(toResponse(_))

    // GET /airports/icao/{icao}
    case GET -> Root / "icao" / icao =>
      algebra.getAirportsBy("icao", icao).flatMap(toResponse(_))

    // GET /airports/city/{city_name} OR
    // GET /airports/city/{city_id}
    case GET -> Root / "city" / city =>
      city.asLong.fold[F[Response[F]]] {
        // Treat city as name
        algebra.getAirportsByCity(city).flatMap(toResponse(_))
      }(algebra.getAirportsBy("city_id", _).flatMap(toResponse(_)))

    // GET /airports/country/{country}
    case GET -> Root / "country" / country =>
      algebra.getAirportsByCountry(country).flatMap(toResponse(_))

    // POST /airports
    case req @ POST -> Root =>
      req
        .attemptAs[AirportCreate]
        .foldF[ApiResult[Long]](
          _ => Applicative[F].pure(Left(EntryInvalidFormat)),
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
          .foldF[ApiResult[Airport]](
            _ => Applicative[F].pure(Left(EntryInvalidFormat)),
            airport =>
              if (id != airport.id) {
                Applicative[F].pure(Left(InconsistentIds(id, airport.id)))
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
            _ => Applicative[F].pure(Left(EntryInvalidFormat)),
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
