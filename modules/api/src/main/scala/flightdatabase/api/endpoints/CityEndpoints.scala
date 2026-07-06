package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase._
import flightdatabase.city.City
import flightdatabase.city.CityAlgebra
import flightdatabase.city.CityCreate
import flightdatabase.country.Country
import flightdatabase.syntax.string._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CityEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CityAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {

    // HEAD /cities/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesCityExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /cities?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[City](sortAndLimit) {
        processReturnOnly[City](_, returnOnly)(algebra.getCities)
      }

    // GET /cities/filter?field={city_field}&operator={operator; default: eq}&value={value}&sort-by={city_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
        FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
        ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[City](sortAndLimit) {
        processFilter[City, City](field, operator, values, _)(algebra.getCitiesBy)
      }

    // GET /cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getCity)

    // GET /cities/country/filter?field={country_field}&operator={operator; default: eq}&value={value}&sort-by={country_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "country" / "filter" :?
        FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
        ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Country](sortAndLimit) {
        processFilter[Country, City](field, operator, values, _)(algebra.getCitiesByCountry)
      }

    // POST /cities
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createCity).flatMap(_.toResponse)

    // PUT /cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[CityCreate, Long](req) { city =>
          if (city.id.exists(_ != i)) {
            InconsistentIds(i, city.id.get).elevate[F, Long]
          } else {
            algebra.updateCity(City.fromCreate(i, city))
          }
        }
      }

    // PATCH /cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateCity(i, _)))

    // DELETE /cities/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeCity)
  }
}

object CityEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CityAlgebra[F]
  ): Endpoints[F] =
    new CityEndpoints(prefix, algebra)
}
