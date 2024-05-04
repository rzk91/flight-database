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

    // GET /airports?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirportsOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getAirports.flatMap(_.toResponse)
      }

    // GET /airports/{valid}?field={airport_field; default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getAirport)
      } else {
        implicitly[TableBase[Airport]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getAirports(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getAirports(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getAirports(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getAirports(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getAirports(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // GET /airports/city/{value}?field={city_field; default: id}
    case GET -> Root / "city" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[City]].fieldTypeMap.get(field) match {
        case Some(StringType)  => algebra.getAirportsByCity(field, value).flatMap(_.toResponse)
        case Some(IntType)     => value.asInt.toResponse(algebra.getAirportsByCity(field, _))
        case Some(LongType)    => value.asLong.toResponse(algebra.getAirportsByCity(field, _))
        case Some(BooleanType) => value.asBoolean.toResponse(algebra.getAirportsByCity(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirportsByCity(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // GET /airports/country/{value}?field={country_field; default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Country]].fieldTypeMap.get(field) match {
        case Some(StringType)  => algebra.getAirportsByCountry(field, value).flatMap(_.toResponse)
        case Some(IntType)     => value.asInt.toResponse(algebra.getAirportsByCountry(field, _))
        case Some(LongType)    => value.asLong.toResponse(algebra.getAirportsByCountry(field, _))
        case Some(BooleanType) => value.asBoolean.toResponse(algebra.getAirportsByCountry(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirportsByCountry(field, _))
        case None => BadRequest(InvalidField(field).error)
      }

    // POST /airports
    case req @ POST -> Root =>
      processRequest(req)(algebra.createAirport).flatMap(_.toResponse)

    // PUT /airports/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequest[AirportCreate, Long](req) { airport =>
          if (airport.id.exists(_ != i)) {
            InconsistentIds(i, airport.id.get).elevate[F, Long]
          } else {
            algebra.updateAirport(Airport.fromCreate(i, airport))
          }
        }
      }

    // PATCH /airports/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequest(req)(algebra.partiallyUpdateAirport(i, _)))

    // DELETE /airports/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeAirport)
  }
}

object AirportEndpoints {

  def apply[F[_]: Concurrent](prefix: String, algebra: AirportAlgebra[F]): Endpoints[F] =
    new AirportEndpoints(prefix, algebra)
}
