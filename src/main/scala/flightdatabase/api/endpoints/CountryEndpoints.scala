package flightdatabase.api.endpoints

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.api._
import flightdatabase.domain.country.CountryAlgebra
import org.http4s._
import org.http4s.circe.CirceEntityCodec._

class CountryEndpoints[F[_]: Concurrent] private (prefix: String, algebra: CountryAlgebra[F])(
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? OnlyNameQueryParamMatcher(onlyNames) =>
      onlyNames match {
        case None | Some(false) => algebra.getCountries.flatMap(toResponse(_))
        case _                  => algebra.getCountriesOnlyNames.flatMap(toResponse(_))
      }
  }
}

object CountryEndpoints {

  def apply[F[_]: Concurrent](
    prefix: String,
    algebra: CountryAlgebra[F]
  )(implicit transactor: Resource[F, HikariTransactor[F]]): Endpoints[F] =
    new CountryEndpoints(prefix, algebra)
}
