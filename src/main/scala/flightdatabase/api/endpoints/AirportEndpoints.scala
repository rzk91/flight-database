package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase.domain._
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
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

    // GET /airports?return-only={airport_field}
    case GET -> Root :? ReturnOnlyMatcher(returnOnly) =>
      processReturnOnly[Airport](returnOnly)(
        stringF = algebra.getAirportsOnly,
        intF = algebra.getAirportsOnly,
        longF = algebra.getAirportsOnly,
        boolF = algebra.getAirportsOnly,
        bigDecimalF = algebra.getAirportsOnly,
        allF = algebra.getAirports
      )

    // GET /airports/filter?field={airport_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airport, Airport](field, operator, values)(
        stringF = algebra.getAirportsBy,
        intF = algebra.getAirportsBy,
        longF = algebra.getAirportsBy,
        boolF = algebra.getAirportsBy,
        bigDecimalF = algebra.getAirportsBy
      )

    // GET /airports/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirport)

    // GET /airports/city/filter?field={city_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[City, Airport](field, operator, values)(
        stringF = algebra.getAirportsByCity,
        intF = algebra.getAirportsByCity,
        longF = algebra.getAirportsByCity,
        boolF = algebra.getAirportsByCity,
        bigDecimalF = algebra.getAirportsByCity
      )

    // GET /airports/country/filter?field={country_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Country, Airport](field, operator, values)(
        stringF = algebra.getAirportsByCountry,
        intF = algebra.getAirportsByCountry,
        longF = algebra.getAirportsByCountry,
        boolF = algebra.getAirportsByCountry,
        bigDecimalF = algebra.getAirportsByCountry
      )

    // POST /airports
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirport).flatMap(_.toResponse)

    // PUT /airports/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirportCreate, Long](req) { airport =>
          if (airport.id.exists(_ != i)) {
            InconsistentIds(i, airport.id.get).elevate[F, Long]
          } else {
            algebra.updateAirport(Airport.fromCreate(i, airport))
          }
        }
      }

    // PATCH /airports/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateAirport(i, _)))

    // DELETE /airports/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirport)
  }
}

object AirportEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirportAlgebra[F]): Endpoints[F] =
    new AirportEndpoints(prefix, algebra)
}
