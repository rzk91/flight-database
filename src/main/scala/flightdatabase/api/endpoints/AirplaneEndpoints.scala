package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class AirplaneEndpoints[F[_]: Concurrent] private (prefix: String, algebra: AirplaneAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override val allowedFields: Set[String] = Set("id", "name", "manufacturer")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // GET /airplanes?only-names
    case GET -> Root :? OnlyNamesFlagMatcher(onlyNames) =>
      if (onlyNames) {
        algebra.getAirplanesOnlyNames.flatMap(toResponse(_))
      } else {
        algebra.getAirplanes.flatMap(toResponse(_))
      }

    // GET /airplanes/name/{name} OR
    // GET /airplanes/id/{id} OR
    // GET /airplanes/manufacturer/{manufacturer_name} OR
    // GET /airplanes/manufacturer/{manufacturer_id}
    case GET -> Root / field / fieldValue if allowedFields(field) =>
      field match {
        case "id" =>
          fieldValue.asLong.fold {
            BadRequest(EntryInvalidFormat.error)
          }(id => algebra.getAirplane(id).flatMap(toResponse(_)))

        case "name" =>
          algebra.getAirplanes(field, fieldValue).flatMap(toResponse(_))

        case "manufacturer" =>
          fieldValue.asLong.fold[F[Response[F]]] {
            algebra.getAirplanesByManufacturer(fieldValue).flatMap(toResponse(_))
          }(id => algebra.getAirplanes("manufacturer_id", id).flatMap(toResponse(_)))
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
