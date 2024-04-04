package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CityEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CityAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  private object CountryQueryParamMatcher
      extends OptionalQueryParamDecoderMatcher[String]("country")

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    // GET /cities?country=Germany&only-names
    case GET -> Root :?
          CountryQueryParamMatcher(maybeCountry) +&
            OnlyNamesFlagMatcher(onlyNames) =>
      val c = maybeCountry.flatMap(_.toOption)
      if (onlyNames) {
        algebra.getCitiesOnlyNames(c).flatMap(toResponse(_))
      } else {
        algebra.getCities(c).flatMap(toResponse(_))
      }
  }
}

object CityEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CityAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): Endpoints[F] =
    new CityEndpoints(prefix, algebra)
}
