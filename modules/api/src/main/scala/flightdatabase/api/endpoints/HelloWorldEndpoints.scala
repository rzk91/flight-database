package flightdatabase.api.endpoints

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.Uri

// Will be converted to `UserAuthRoutes` in the future
class HelloWorldEndpoints[F[_]: Monad](prefix: String, flightDbBaseUri: Uri)
    extends Endpoints[F](prefix) {

  override val endpoints: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / name =>
      Ok {
        s"Hello, $name! Check out our amazing flight database!\n" +
        s"For example, look up all the airplanes: ${flightDbBaseUri.renderString}/airplanes\n" +
        s"Or look up all the airlines: ${flightDbBaseUri.renderString}/airlines.\n"
      }
  }
}

object HelloWorldEndpoints {

  def apply[F[_]: Monad](prefix: String, flightDbBaseUri: Uri): Endpoints[F] =
    new HelloWorldEndpoints(prefix, flightDbBaseUri)
}
