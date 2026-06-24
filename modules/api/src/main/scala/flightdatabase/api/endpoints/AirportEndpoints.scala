package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import flightdatabase._
import flightdatabase.airport.Airport
import flightdatabase.airport.AirportAlgebra
import flightdatabase.airport.AirportCreate
import flightdatabase.city.City
import flightdatabase.country.Country
import flightdatabase.syntax.string._
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

    // GET /airports?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Airport](sortAndLimit) {
        processReturnOnly[Airport](_, returnOnly)(algebra.getAirports)
      }

    // GET /airports/filter?field={airport_field}&operator={operator; default: eq}&value={value}&sort-by={airport_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airport](sortAndLimit) {
        processFilter[Airport, Airport](field, operator, values, _)(algebra.getAirportsBy)
      }

    // GET /airports/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirport)

    // GET /airports/city/filter?field={city_field}&operator={operator; default: eq}&value={value}&sort-by={city_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[City](sortAndLimit) {
        processFilter[City, Airport](field, operator, values, _)(algebra.getAirportsByCity)
      }

    // GET /airports/country/filter?field={country_field}&operator={operator; default: eq}&value={value}&sort-by={country_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processFilter[Country, Airport](field, operator, values, _)(algebra.getAirportsByCountry)
      }

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
