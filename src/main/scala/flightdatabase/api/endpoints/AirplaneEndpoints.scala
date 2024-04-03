package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.utils.implicits._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirplaneAlgebra[F])(
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
        case None | Some(false) => algebra.getAirplanes(m).flatMap(toResponse(_))
        case _                  => algebra.getAirplanesOnlyNames(m).flatMap(toResponse(_))
      }
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): HttpRoutes[F] =
    new AirplaneEndpoints(prefix, algebra).routes
}
