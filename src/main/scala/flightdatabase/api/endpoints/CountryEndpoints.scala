package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.model.FlightDbTable._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CountryEndpoints[F[_]: Concurrent] private (prefix: String)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root => getStringList(COUNTRY).execute.flatMap(toResponse(_))
  }
}

object CountryEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new CountryEndpoints(prefix).routes
}
