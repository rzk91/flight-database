package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
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
      withFieldValidation[Airport](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getAirport)
          case "city_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirports(field, _))
          case _ => algebra.getAirports(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airports/city/{value}?field={city_field; default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[City](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirportsByCity(field, _))
        } else {
          algebra.getAirportsByCity(field, value).flatMap(toResponse(_))
        }
      }

    // GET /airports/country/{value}?field={country_field; default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Country](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(algebra.getAirportsByCountry(field, _))
        } else {
          algebra.getAirportsByCountry(field, value).flatMap(toResponse(_))
        }
      }

    // POST /airports
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirport).flatMap(toResponse(_))

    // PUT /airports/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[Airport, Long](req) { airport =>
          if (i != airport.id) {
            InconsistentIds(i, airport.id).elevate[F, Long]
          } else {
            algebra.updateAirport(airport)
          }
        }
      }

    // PATCH /airports/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateAirport(i, _)))

    // DELETE /airports/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeAirport)
  }
}

object AirportEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirportAlgebra[F]): Endpoints[F] =
    new AirportEndpoints(prefix, algebra)
}
