package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase._
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyAlgebra
import flightdatabase.currency.CurrencyCreate
import flightdatabase.extensions.string._
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

    // GET /currencies?return-only={field}&sort-by={field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root :? SortAndLimit(sortAndLimit) +& ReturnOnlyMatcher(returnOnly) =>
      withSortAndLimitValidation[Currency](sortAndLimit) {
        processReturnOnly2[Currency](_, returnOnly)(algebra.getCurrencies)
      }

    // GET /currencies/filter?field={currency_field}&operator={operator; default: eq}&value={value}&sort-by={currency_field}&order={asc, desc}&limit={number}&offset={number}
    case GET -> Root / "filter" :?
          FieldMatcher(field) +& OperatorMatcherEqDefault(operator) +&
            ValueMatcher(values) +& SortAndLimit(sortAndLimit) =>
      withSortAndLimitValidation[Currency](sortAndLimit) {
        processFilter2[Currency, Currency](field, operator, values, _)(algebra.getCurrenciesBy)
      }

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
