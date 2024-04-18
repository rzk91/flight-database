package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.InconsistentIds
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyCreate
import flightdatabase.domain.currency.CurrencyPatch
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CurrencyEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CurrencyAlgebra[F])
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {

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

    // GET /currencies/{id}
    case GET -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.getCurrency(id).flatMap(toResponse(_)))

    // GET /currencies/name/{name}
    case GET -> Root / "name" / name =>
      algebra.getCurrencies("name", name).flatMap(toResponse(_))

    // GET /currencies/iso/{iso}
    case GET -> Root / "iso" / iso =>
      algebra.getCurrencies("iso", iso).flatMap(toResponse(_))

    // POST /currencies
    case req @ POST -> Root =>
      req
        .attemptAs[CurrencyCreate]
        .foldF[ApiResult[Long]](
          _ => EntryInvalidFormat.elevate[F, Long],
          algebra.createCurrency
        )
        .flatMap(toResponse(_))

    // PUT /currencies/{id}
    case req @ PUT -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[Currency]
          .foldF[ApiResult[Long]](
            _ => EntryInvalidFormat.elevate[F, Long],
            currency =>
              if (id != currency.id) {
                InconsistentIds(id, currency.id).elevate[F, Long]
              } else {
                algebra.updateCurrency(currency)
              }
          )
          .flatMap(toResponse(_))
      }

    // PATCH /currencies/{id}
    case req @ PATCH -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      } { id =>
        req
          .attemptAs[CurrencyPatch]
          .foldF[ApiResult[Currency]](
            _ => EntryInvalidFormat.elevate[F, Currency],
            algebra.partiallyUpdateCurrency(id, _)
          )
          .flatMap(toResponse(_))
      }

    // DELETE /currencies/{id}
    case DELETE -> Root / id =>
      id.asLong.fold {
        BadRequest(EntryInvalidFormat.error)
      }(id => algebra.removeCurrency(id).flatMap(toResponse(_)))
  }
}

object CurrencyEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CurrencyAlgebra[F]
  ): Endpoints[F] =
    new CurrencyEndpoints(prefix, algebra)
}
