package flightdatabase.api.services

import cats.effect._
import cats.implicits._
import flightdatabase.api._
import flightdatabase.db.DbMethods._
import flightdatabase.db._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

class CountryService[F[_]: Async] extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "countries" =>
      getStringList("country").execute.flatMap(toResponse[F, List[String]])
  }
}

object CountryService {
  def apply[F[_]: Async]: HttpRoutes[F] = new CountryService().service
}
