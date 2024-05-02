package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.country.Country
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
        algebra.getCitiesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getCities.flatMap(toResponse(_))
      }

    // GET /cities/{value}?field={city_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[City](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getCity)
          case "country_id" =>
            idToResponse(value, EntryHasInvalidForeignKey)(algebra.getCities(field, _))
          case _ => algebra.getCities(field, value).flatMap(toResponse(_))
        }
      }

    // GET /cities/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Country](field) {
        if (field.endsWith("id")) {
          idToResponse(value, EntryHasInvalidForeignKey)(algebra.getCitiesByCountry(field, _))
        } else {
          algebra.getCitiesByCountry(field, value).flatMap(toResponse(_))
        }
      }

    // POST /cities
    case req @ POST -> Root =>
      processRequest(req)(algebra.createCity).flatMap(toResponse(_))

    // PUT /cities/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[City, Long](req) { city =>
          if (i != city.id) {
            InconsistentIds(i, city.id).elevate[F, Long]
          } else {
            algebra.updateCity(city)
          }
        }
      }

    // PATCH /cities/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateCity(i, _)))

    // DELETE /cities/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeCity)
  }
}

object CityEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CityAlgebra[F]
  ): Endpoints[F] =
    new CityEndpoints(prefix, algebra)
}
