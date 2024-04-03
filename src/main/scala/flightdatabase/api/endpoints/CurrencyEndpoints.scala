package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.currency.CurrencyAlgebra
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CurrencyEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CurrencyAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => algebra.getCurrencies.flatMap(toResponse(_))
        case _                  => algebra.getCurrenciesOnlyNames.flatMap(toResponse(_))
      }
  }
}

object CurrencyEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CurrencyAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new CurrencyEndpoints(prefix, algebra).routes
}
