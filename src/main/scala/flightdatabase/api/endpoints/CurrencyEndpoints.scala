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

    // GET /currencies/filter?field={currency_field}&operator={operator; default: eq}&value={value}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +& ValueMatcher(values) =>
      processFilter[Currency, Currency](field, operator, values)(
        stringF = algebra.getCurrenciesBy,
        intF = algebra.getCurrenciesBy,
        longF = algebra.getCurrenciesBy,
        boolF = algebra.getCurrenciesBy,
        bigDecimalF = algebra.getCurrenciesBy
      )

    // GET /currencies/{id}
    case GET -> Root / id =>
      id.asLong.toResponse(algebra.getCurrency)

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
