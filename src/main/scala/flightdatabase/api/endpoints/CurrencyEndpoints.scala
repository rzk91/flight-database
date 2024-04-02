package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.domain.FlightDbTable.CURRENCY
import flightdatabase.utils.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CurrencyEndpoints[F[_]: Concurrent] private (prefix: String)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => getCurrencies.execute.flatMap(toResponse(_))
        case _                  => getStringList(CURRENCY).execute.flatMap(toResponse(_))
      }
  }
}

object CurrencyEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new CurrencyEndpoints(prefix).routes
}
