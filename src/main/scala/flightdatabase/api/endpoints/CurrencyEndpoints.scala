package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyCreate
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
        algebra.getCurrenciesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getCurrencies.flatMap(toResponse(_))
      }

    // GET /currencies/{value}?field={currency_field; default=id}
    case GET -> Root / value :? FieldMatcherIdDefault(field) =>
      withFieldValidation[Currency](field) {
        field match {
          case "id" => idToResponse(value)(algebra.getCurrency)
          case _    => algebra.getCurrencies(field, value).flatMap(toResponse(_))
        }
      }

    // POST /currencies
    case req @ POST -> Root =>
      processRequest(req)(algebra.createCurrency).flatMap(toResponse(_))

    // PUT /currencies/{id}
    case req @ PUT -> Root / id =>
      idToResponse(id) { i =>
        processRequest[CurrencyCreate, Long](req) { currency =>
          if (currency.id.exists(_ != i)) {
            InconsistentIds(i, currency.id.get).elevate[F, Long]
          } else {
            algebra.updateCurrency(Currency.fromCreate(i, currency))
          }
        }
      }

    // PATCH /currencies/{id}
    case req @ PATCH -> Root / id =>
      idToResponse(id)(i => processRequest(req)(algebra.partiallyUpdateCurrency(i, _)))

    // DELETE /currencies/{id}
    case DELETE -> Root / id =>
      idToResponse(id)(algebra.removeCurrency)
  }
}

object CurrencyEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CurrencyAlgebra[F]
  ): Endpoints[F] =
    new CurrencyEndpoints(prefix, algebra)
}
