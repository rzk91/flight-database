package flightdatabase.api.endpoints

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

abstract class Endpoints[F[_]: Monad](prefix: String) extends Http4sDsl[F] {

  implicit val dsl: Http4sDslT[F] = Http4sDsl.apply[F]
  def endpoints: HttpRoutes[F]
  def routes: HttpRoutes[F] = Router(prefix -> endpoints)
}
