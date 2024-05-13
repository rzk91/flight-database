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

    // GET /airlines?return-only={field}
    case GET -> Root :? ReturnOnlyMatcher(onlyField) => {
        for {
          field     <- onlyField
          fieldType <- implicitly[TableBase[Airline]].fieldTypeMap.get(field)
        } yield {
          fieldType match {
            case StringType  => algebra.getAirlinesOnly[String](field).flatMap(_.toResponse[F])
            case IntType     => algebra.getAirlinesOnly[Int](field).flatMap(_.toResponse[F])
            case LongType    => algebra.getAirlinesOnly[Long](field).flatMap(_.toResponse[F])
            case BooleanType => algebra.getAirlinesOnly[Boolean](field).flatMap(_.toResponse[F])
            case BigDecimalType =>
              algebra.getAirlinesOnly[BigDecimal](field).flatMap(_.toResponse[F])
          }
        }
      }.getOrElse(algebra.getAirlines.flatMap(_.toResponse[F]))

    // GET /airlines/filter?field={airline_field}&operator={operator, default: eq}&value={values}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Airline, Airline](field, operator, values)(
        stringF = algebra.getAirlinesBy,
        intF = algebra.getAirlinesBy,
        longF = algebra.getAirlinesBy,
        boolF = algebra.getAirlinesBy,
        bigDecimalF = algebra.getAirlinesBy
      )

    // GET /airlines/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getAirline)

    // GET /airlines/country/filter?field={country_field}&operator={operator, default: eq}&value={value}
    case GET -> Root / "country" / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Country, Airline](field, operator, values)(
        stringF = algebra.getAirlinesByCountry,
        intF = algebra.getAirlinesByCountry,
        longF = algebra.getAirlinesByCountry,
        boolF = algebra.getAirlinesByCountry,
        bigDecimalF = algebra.getAirlinesByCountry
      )

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
