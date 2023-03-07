package flightdatabase

import cats.effect._
import com.comcast.ip4s.{Host, Port}
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Server => ApiServer}

abstract class Server[F[_]: Async] {
  def host: Option[Host]
  def port: Port
  def httpApp: HttpApp[F]

  def server: Resource[F, ApiServer] =
    EmberServerBuilder
      .default[F]
      .withHostOption(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .build
}
