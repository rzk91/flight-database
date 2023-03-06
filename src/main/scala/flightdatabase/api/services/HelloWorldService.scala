package flightdatabase.api.services

import cats.Monad
import flightdatabase.config.Configuration.flightDbBaseUri
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class HelloWorldService[F[_]: Monad] extends Http4sDsl[F] {

  def service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / name => Ok {
      s"Hello, $name! Check out our amazing flight database!\n" +
      s"For example, look up all the airplanes: ${flightDbBaseUri.renderString}/airplanes"
    }
  }
}

object HelloWorldService {
  def apply[F[_]: Monad]: HttpRoutes[F] = new HelloWorldService().service
}
