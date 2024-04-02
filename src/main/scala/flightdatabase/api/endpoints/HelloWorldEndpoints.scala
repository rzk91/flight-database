package flightdatabase.api.endpoints

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.Uri

class HelloWorldEndpoints[F[_]: Monad](prefix: String, flightDbBaseUri: Uri)
    extends Endpoints[F](prefix) {

  override def endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / name =>
      Ok {
        s"Hello, $name! Check out our amazing flight database!\n" +
        s"For example, look up all the airplanes: ${flightDbBaseUri.renderString}/airplanes"
      }
  }
}

object HelloWorldEndpoints {

  def apply[F[_]: Monad](prefix: String, flightDbBaseUri: Uri): HttpRoutes[F] =
    new HelloWorldEndpoints(prefix, flightDbBaseUri).routes
}
