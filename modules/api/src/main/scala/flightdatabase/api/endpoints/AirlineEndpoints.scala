package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase._
import flightdatabase.airline.Airline
import flightdatabase.airline.AirlineAlgebra
import flightdatabase.airline.AirlineCreate
import flightdatabase.country.Country
import flightdatabase.syntax.string._
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

    // GET /airlines?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Airline](sortAndLimit) {
        processReturnOnly[Airline](_, returnOnly)(algebra.getAirlines)
      }

    // GET /airlines/{id}
    case GET -> Root / LongVar(id) =>
      algebra.getAirline(id).flatMap(_.toResponse)

    // GET /airlines/filter?field={airline_field}&operator={operator, default: eq}&value={values}&sort-by={airline_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
        FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
        ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Airline](sortAndLimit) {
        processFilter[Airline, Airline](field, operator, values, _)(algebra.getAirlinesBy)
      }

    // GET /airlines/country/filter?field={country_field}&operator={operator, default: eq}&value={value}&sort-by={country_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "country" / "filter" :?
        FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
        ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processFilter[Country, Airline](field, operator, values, _)(algebra.getAirlinesByCountry)
      }

    // POST /airlines
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createAirline).flatMap(_.toResponse)

    // PUT /airlines/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[AirlineCreate, Long](req) { airline =>
          if (airline.id.exists(_ != i)) {
            InconsistentIds(i, airline.id.get).elevate[F, Long]
          } else {
            algebra.updateAirline(Airline.fromCreate(i, airline))
          }
        }
      }

    // PATCH /airlines/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateAirline(i, _)))

    // DELETE /airlines/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirline)
  }
}

object AirlineEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineAlgebra[F]): Endpoints[F] =
    new AirlineEndpoints[F](prefix, algebra)
}
