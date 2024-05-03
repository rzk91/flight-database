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

    // GET /airline-cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      (airlineId.asLong, cityId.asLong).tupled.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/{value}?field={airline_field, default: id}
    case GET -> Root / "airline" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Airline]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineCitiesByAirline(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getAirlineCitiesByAirline(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getAirlineCitiesByAirline(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineCitiesByAirline(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineCitiesByAirline(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /airline-cities/city/{value}?field={city_field, default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[City]].fieldTypeMap.get(field) match {
        case Some(StringType) =>
          algebra.getAirlineCitiesByCity(field, value).flatMap(_.toResponse)
        case Some(IntType)  => value.asInt.toResponse(algebra.getAirlineCitiesByCity(field, _))
        case Some(LongType) => value.asLong.toResponse(algebra.getAirlineCitiesByCity(field, _))
        case Some(BooleanType) =>
          value.asBoolean.toResponse(algebra.getAirlineCitiesByCity(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlineCitiesByCity(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /airline-cities
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirlineCity).flatMap(_.toResponse)

    // PUT /airline-cities/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[AirlineCityCreate, Long](req) { airlineCity =>
          if (airlineCity.id.exists(_ != i)) {
            InconsistentIds(i, airlineCity.id.get).elevate[F, Long]
          } else {
            algebra.updateAirlineCity(AirlineCity.fromCreate(i, airlineCity))
          }
        }
      }

    // PATCH /airline-cities/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateAirlineCity(i, _)))

    // DELETE /airline-cities/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirlineCity)
  }
}

object AirlineCityEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirlineCityAlgebra[F]): Endpoints[F] =
    new AirlineCityEndpoints(prefix, algebra)
}
