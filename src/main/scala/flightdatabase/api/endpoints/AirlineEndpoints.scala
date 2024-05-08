package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.country.Country
import flightdatabase.utils.implicits.enrichString
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

    // GET /airlines?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirlinesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getAirlines.flatMap(_.toResponse)
      }

    // GET /airlines/{value}?field={airline_field, default: id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getAirline)
      } else {
        implicitly[TableBase[Airline]].fieldTypeMap.get(field) match {
          case Some(StringType)     => algebra.getAirlines(field, value).flatMap(_.toResponse)
          case Some(IntType)        => value.asInt.toResponse(algebra.getAirlines(field, _))
          case Some(LongType)       => value.asLong.toResponse(algebra.getAirlines(field, _))
          case Some(BooleanType)    => value.asBoolean.toResponse(algebra.getAirlines(field, _))
          case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra.getAirlines(field, _))
          case None                 => BadRequest(InvalidField(field).error)
        }
      }

    // GET /airlines/country/{value}?field={country_field, default: id}
    case GET -> Root / "country" / value :? FieldMatcherIdDefault(field) =>
      implicitly[TableBase[Country]].fieldTypeMap.get(field) match {
        case Some(StringType)  => algebra.getAirlinesByCountry(field, value).flatMap(_.toResponse)
        case Some(IntType)     => value.asInt.toResponse(algebra.getAirlinesByCountry(field, _))
        case Some(LongType)    => value.asLong.toResponse(algebra.getAirlinesByCountry(field, _))
        case Some(BooleanType) => value.asBoolean.toResponse(algebra.getAirlinesByCountry(field, _))
        case Some(BigDecimalType) =>
          value.asBigDecimal.toResponse(algebra.getAirlinesByCountry(field, _))
        case None => BadRequest(InvalidField(field).error)
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
