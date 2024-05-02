package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.country.Country
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
      withFieldValidation[Airline](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getAirline)
          case "country_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlines(field, _))
          case _ => algebra.getAirlines(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airlines/country/{value}?field={country_field, default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Country](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirlinesByCountry(field, _))
        } else {
          algebra.getAirlinesByCountry(field, value).flatMap(toResponse(_))
        }
      }

    // POST /airlines
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirline).flatMap(toResponse(_))

    // PUT /airlines/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Airline, Long](req) { airline =>
          if (i != airline.id) {
            InconsistentIds(i, airline.id).elevate[F, Long]
          } else {
            algebra.updateAirline(airline)
          }
        }
      }

    // PATCH /airlines/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirline(i, _)))

    // DELETE /airlines/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirline)
  }
}

object AirlineEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAlgebra[F]): Endpoints[F] =
    new AirlineEndpoints[F](prefix, algebra)
}
