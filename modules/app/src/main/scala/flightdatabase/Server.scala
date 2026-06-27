package flightdatabase

import cats.effect._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Server => ApiServer}

object Server {

  /**
    * Starts the API server with the specified host, port, and HTTP app.
    *
    * @param host     An optional host to bind the server to.
    * @param port     The port number to listen on.
    * @param httpApp  The HTTP app to handle incoming requests.
    * @tparam F The effect type, which must have an instance of `Async`.
    * @return         A resource representing the running API server.
    */
  def start[F[_]: Async](
    host: Option[Host],
    port: Port,
    httpApp: HttpApp[F]
  ): Resource[F, ApiServer] = {
    implicit val network: Network[F] = Network.forAsync[F]
    EmberServerBuilder
      .default[F]
      .withHostOption(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .build
  }
}
