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
            OnlyNamesFlagMatcher(onlyNames) =>
      val m = maybeManufacturer.flatMap(_.toOption)
      if (onlyNames) {
        algebra.getAirplanesOnlyNames(m).flatMap(toResponse(_))
      } else {
        algebra.getAirplanes(m).flatMap(toResponse(_))
      }
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): Endpoints[F] =
    new AirplaneEndpoints(prefix, algebra)
}
