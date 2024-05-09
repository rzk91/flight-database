package flightdatabase.api.endpoints

import cats.effect.Concurrent
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityAlgebra
import flightdatabase.domain.airline_city.AirlineCityCreate
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

    // GET /airline-cities/filter?field={airline_city_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineCity](field, op) {
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCities(field, _, op)
          )
      }

    // GET /airline-cities/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/{airline_id}/city/{city_id}
    case GET -> Root / "airline" / airlineId / "city" / cityId =>
      (airlineId.asLong, cityId.asLong).tupled.toResponse(algebra.getAirlineCity)

    // GET /airline-cities/airline/filter?field={airline_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "airline" / "filter" :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineCity](field, op) {
        case Some(StringType) if StringType.operators(op) =>
          values.asStringToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByAirline(field, _, op)
          )
        case Some(IntType) if IntType.operators(op) =>
          values.asIntToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByAirline(field, _, op)
          )
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByAirline(field, _, op)
          )
        case Some(BooleanType) if BooleanType.operators(op) =>
          values.asBooleanToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByAirline(field, _, op)
          )
        case Some(BigDecimalType) if BigDecimalType.operators(op) =>
          values.asBigDecimalToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByAirline(field, _, op)
          )
      }

    // GET /airline-cities/city/filter?field={city_field; default: id}&operator={operator; default: eq}&value={value}
    case GET -> Root / "city" / "filter" :?
          FieldMatcherIdDefault(field) +& OperatorMatcherEqDefault(op) +& ValueMatcher(values) =>
      processFilter[AirlineCity](field, op) {
        case Some(StringType) if StringType.operators(op) =>
          values.asStringToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByCity(field, _, op)
          )
        case Some(IntType) if IntType.operators(op) =>
          values.asIntToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByCity(field, _, op)
          )
        case Some(LongType) if LongType.operators(op) =>
          values.asLongToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByCity(field, _, op)
          )
        case Some(BooleanType) if BooleanType.operators(op) =>
          values.asBooleanToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByCity(field, _, op)
          )
        case Some(BigDecimalType) if BigDecimalType.operators(op) =>
          values.asBigDecimalToResponse[F, List[AirlineCity]](field, op)(
            algebra.getAirlineCitiesByCity(field, _, op)
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
