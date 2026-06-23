package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase._
import flightdatabase.airline.Airline
import flightdatabase.airline_city.AirlineCity
import flightdatabase.airline_city.AirlineCityAlgebra
import flightdatabase.airline_city.AirlineCityCreate
import flightdatabase.city.City
import flightdatabase.syntax.string._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirlineCityEndpoints[F[_]: Concurrent] private (
  prefix: String,
  algebra: AirlineCityAlgebra[F]
) extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    // HEAD /airline-cities/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesAirlineCityExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /airline-cities?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[AirlineCity](sortAndLimit) {
        processReturnOnly[AirlineCity](_, returnOnly)(algebra.getAirlineCities)
      }

    // GET /airline-cities/filter?field={airline_city_field}&operator={operator; default: eq}&value={value}&sort-by={airline_city_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[AirlineCity](sortAndLimit) {
        processFilter[AirlineCity, AirlineCity](field, operator, values, _)(
          algebra.getAirlineCitiesBy
        )
      }

    // GET /airline-cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      (airlineId.asLong, cityId.asLong).tupled.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}&sort-by={airline_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airline](sortAndLimit) {
        processFilter[Airline, AirlineCity](field, operator, values, _)(
          algebra.getAirlineCitiesByAirline
        )
      }

    // GET /airline-cities/city/filter?field={city_field}&operator={operator; default: eq}&value={value}&sort-by={city_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[City](sortAndLimit) {
        processFilter[City, AirlineCity](field, operator, values, _)(
          algebra.getAirlineCitiesByCity
        )
      }

    // POST /airline-cities
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirlineCity).flatMap(_.toResponse)

    // PUT /airline-cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirlineCityCreate, Long](req) { airlineCity =>
          if (airlineCity.id.exists(_ != i)) {
            InconsistentIds(i, airlineCity.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineCity(AirlineCity.fromCreate(i, airlineCity))
          }
        }
      }

    // PATCH /airline-cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateAirlineCity(i, _)))

    // DELETE /airline-cities/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirlineCity)
  }
}

object AirlineCityEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineCityAlgebra[F]): Endpoints[F] =
    new AirlineCityEndpoints(prefix, algebra)
}
