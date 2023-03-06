package flightdatabase.api.services

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import flightdatabase.utils.CollectionsHelper._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

class CityService[F[_]: Async] extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]

  object CountryQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("country")

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "cities" :? CountryQueryParamMatcher(maybeCountry) =>
      getStringListBy("city", "country", maybeCountry.flatMap(_.toOption)).execute
        .flatMap(toResponse[F, List[String]])
  }
}

object CityService {
  def apply[F[_]: Async]: HttpRoutes[F] = new CityService().service
}
