package flightdatabase.api

import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import flightdatabase.api.ApiEndpoints._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router

object ApiMain extends IOApp {

  val services = helloWorldService <+> flightDbService
  val httpApp = Router("/" -> helloWorldService, "/flightdb" -> services).orNotFound

  val server = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"18181")
    .withHttpApp(httpApp)
    .build

  def run(args: List[String]): IO[ExitCode] =
    server
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
