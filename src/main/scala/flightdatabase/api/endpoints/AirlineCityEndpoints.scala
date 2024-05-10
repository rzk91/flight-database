package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityAlgebra
import flightdatabase.domain.airline_city.AirlineCityCreate
import flightdatabase.domain.city.City
import flightdatabase.utils.implicits.enrichString
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

    // GET /airline-cities
    case GET -> Root =>
      algebra.getAirlineCities.flatMap(_.toResponse)

    // GET /airline-cities/filter?field={airline_city_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[AirlineCity, AirlineCity](field, operator, values)(
        stringF = algebra.getAirlineCitiesBy,
        intF = algebra.getAirlineCitiesBy,
        longF = algebra.getAirlineCitiesBy,
        boolF = algebra.getAirlineCitiesBy,
        bigDecimalF = algebra.getAirlineCitiesBy
      )

    // GET /airline-cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      (airlineId.asLong, cityId.asLong).tupled.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/filter?field={airline_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airline, AirlineCity](field, operator, values)(
        stringF = algebra.getAirlineCitiesByAirline,
        intF = algebra.getAirlineCitiesByAirline,
        longF = algebra.getAirlineCitiesByAirline,
        boolF = algebra.getAirlineCitiesByAirline,
        bigDecimalF = algebra.getAirlineCitiesByAirline
      )

    // GET /airline-cities/city/filter?field={city_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "city" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[City, AirlineCity](field, operator, values)(
        stringF = algebra.getAirlineCitiesByCity,
        intF = algebra.getAirlineCitiesByCity,
        longF = algebra.getAirlineCitiesByCity,
        boolF = algebra.getAirlineCitiesByCity,
        bigDecimalF = algebra.getAirlineCitiesByCity
      )

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
