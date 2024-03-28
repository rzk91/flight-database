package flightdatabase.api.services

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.model.FlightDbTable._
import flightdatabase.model.objects.Airplane
import flightdatabase.utils.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

class AirplaneService[F[_]: Async](implicit transactor: Resource[F, HikariTransactor[F]])
    extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]

  object ManufacturerQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("manufacturer")

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "airplanes" :?
          ManufacturerQueryParamMatcher(maybeManufacturer) +&
            OnlyNameQueryParamMatcher(onlyNames) =>
      val m = maybeManufacturer.flatMap(_.toOption)
      onlyNames match {
        case None | Some(false) => getAirplanes(m).execute.flatMap(toResponse(_))
        case _                  => getStringListBy(AIRPLANE, MANUFACTURER, m).execute.flatMap(toResponse(_))
      }
  }

}

object AirplaneService {

  def apply[F[_]: Async](implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new AirplaneService().service
}
