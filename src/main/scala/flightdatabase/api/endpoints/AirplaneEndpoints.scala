package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.airplane.AirplaneAlgebra
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirplaneAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // GET /airplanes?onlyNames
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirplanesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getAirplanes.flatMap(toResponse(_))
      }

    // GET /airplanes/manufacturer/{manufacturer_name}
    case GET -> Root / "manufacturer" / manufacturer =>
      algebra.getAirplanesByManufacturer(manufacturer).flatMap(toResponse(_))
  }

}

object AirplaneEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: AirplaneAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): Endpoints[F] =
    new AirplaneEndpoints(prefix, algebra)
}
