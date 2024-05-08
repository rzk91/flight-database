package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain._
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyCreate
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CurrencyEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CurrencyAlgebra[F])
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {

    // HEAD /currencies/{id}
    case HEAD -> Root / LongVar(id) =>
      algebra.doesCurrencyExist(id).flatMap {
        case true  => Ok()
        case false => NotFound()
      }

    // GET /currencies?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getCurrenciesOnlyNames.flatMap(_.toResponse)
      } else {
        algebra.getCurrencies.flatMap(_.toResponse)
      }

    // GET /currencies/{value}?field={currency_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      if (field == "id") {
        value.asLong.toResponse(algebra.getCurrency)
      } else {
        implicitly[TableBase[Currency]].fieldTypeMap.get(field) match {
          case Some(StringType)  => algebra.getCurrencies(field, value).flatMap(_.toResponse)
          case Some(IntType)     => value.asInt.toResponse(algebra.getCurrencies(field, _))
          case Some(LongType)    => value.asLong.toResponse(algebra.getCurrencies(field, _))
          case Some(BooleanType) => value.asBoolean.toResponse(algebra.getCurrencies(field, _))
          case Some(BigDecimalType) =>
            value.asBigDecimal.toResponse(algebra.getCurrencies(field, _))
          case None => BadRequest(InvalidField(field).error)
        }
      }

    // POST /currencies
    case req @ POST -> Root =>
      processRequestBody(req)(algebra.createCurrency).flatMap(_.toResponse)

    // PUT /currencies/{id}
    case req @ PUT -> Root / id =>
      id.asLong.toResponse { i =>
        processRequestBody[CurrencyCreate, Long](req) { currency =>
          if (currency.id.exists(_ != i)) {
            InconsistentIds(i, currency.id.get).elevate[F, Long]
          } else {
            algebra.updateCurrency(Currency.fromCreate(i, currency))
          }
        }
      }

    // PATCH /currencies/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.toResponse(i => processRequestBody(req)(algebra.partiallyUpdateCurrency(i, _)))

    // DELETE /currencies/{id}
    case DELETE -> Root / id =>
      id.asLong.toResponse(algebra.removeCurrency)
  }
}

object CurrencyEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CurrencyAlgebra[F]
  ): Endpoints[F] =
    new CurrencyEndpoints(prefix, algebra)
}
