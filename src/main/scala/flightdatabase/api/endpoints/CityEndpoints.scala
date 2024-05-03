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

    // GET /cities/{value}?field={city_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getCity)
      } else {
        implicitly[TableBase[City]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getCities(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getCities(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getCities(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getCities(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getCities(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // GET /cities/country/{value}?field={country_field; default=id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Country]].fieldTypeMap.get(field) match {
        case Some(StringType)  => algebra.getCitiesByCountry(field, value).flatMap(_.toResponse)
        case Some(IntType)     => value.asInt.toResponse(algebra.getCitiesByCountry(field, _))
        case Some(LongType)    => value.asLong.toResponse(algebra.getCitiesByCountry(field, _))
        case Some(BooleanType) => value.asBoolean.toResponse(algebra.getCitiesByCountry(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getCitiesByCountry(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /cities
    case req @ POST -> Root =>
      processRequest(req)(algebra.createCity).flatMap(_.toResponse)

    // PUT /cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[CityCreate, Long](req) { city =>
          if (city.id.exists(_ != i)) {
            InconsistentIds(i, city.id.get).elevate[F, Long]
          } else {
            algebra.updateCity(City.fromCreate(i, city))
          }
        }
      }

    // PATCH /cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateCity(i, _)))

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
