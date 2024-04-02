package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.model.FlightDbTable._
import flightdatabase.utils.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CityEndpoints[F[_]: Concurrent] private (prefix: String)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  private object CountryQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("country")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? CountryQueryParamMatcher(maybeCountry) =>
      getStringListBy(CITY, COUNTRY, maybeCountry.flatMap(_.toOption)).execute
        .flatMap(toResponse(_))
  }
}

object CityEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new CityEndpoints(prefix).routes
}
