package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityCreate
import flightdatabase.domain.country.Country
import flightdatabase.utils.implicits.enrichString
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

    // GET /cities?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getCitiesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getCities.flatMap(_.toResponse)
      }

    // GET /cities/filter?field={city_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[City, City](field, operator, values)(
        stringF = algebra.getCitiesBy,
        intF = algebra.getCitiesBy,
        longF = algebra.getCitiesBy,
        boolF = algebra.getCitiesBy,
        bigDecimalF = algebra.getCitiesBy
      )

    // GET /cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getCity)

    // GET /cities/country/filter?field={country_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Country, City](field, operator, values)(
        stringF = algebra.getCitiesByCountry,
        intF = algebra.getCitiesByCountry,
        longF = algebra.getCitiesByCountry,
        boolF = algebra.getCitiesByCountry,
        bigDecimalF = algebra.getCitiesByCountry
      )

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
