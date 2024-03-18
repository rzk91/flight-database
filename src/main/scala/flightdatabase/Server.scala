package flightdatabase

import cats.effect._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Server => ApiServer}

object Server {

  def start[F[_]: Async](host: Option[Host], port: Port, httpApp: HttpApp[F]): Resource[F, ApiServer] =
    EmberServerBuilder
      .default[F]
      .withHostOption(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .build
}
