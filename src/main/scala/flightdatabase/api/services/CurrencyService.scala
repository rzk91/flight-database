package flightdatabase.api.services

import cats._
import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.model.FlightDbTable.CURRENCY
import flightdatabase.model.objects.Currency
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

class CurrencyService[F[_]: Async] extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "currencies" :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => getCurrencies.execute.flatMap(toResponse(_))
        case _                  => getStringList(CURRENCY).execute.flatMap(toResponse(_))
      }
  }
}

object CurrencyService {
  def apply[F[_]: Async]: HttpRoutes[F] = new CurrencyService[F].service
}
