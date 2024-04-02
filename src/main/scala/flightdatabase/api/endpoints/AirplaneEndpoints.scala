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

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String)(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  object ManufacturerQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("manufacturer")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :?
          ManufacturerQueryParamMatcher(maybeManufacturer) +&
            OnlyNameQueryParamMatcher(onlyNames) =>
      val m = maybeManufacturer.flatMap(_.toOption)
      onlyNames match {
        case None | Some(false) => getAirplanes(m).execute.flatMap(toResponse(_))
        case _                  => getStringListBy(AIRPLANE, MANUFACTURER, m).execute.flatMap(toResponse(_))
      }
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new AirplaneEndpoints(prefix).routes
}
