package flightdatabase.api

import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import flightdatabase.api.ApiEndpoints._
import flightdatabase.config.Configuration.apiConfig
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router

object ApiMain extends IOApp {

  val services = helloWorldService <+> flightDbService
  val httpApp = Router("/" -> helloWorldService, "/flightdb" -> services).orNotFound

  val server = EmberServerBuilder
    .default[IO]
    .withHostOption(apiConfig.hostName)
    .withPort(apiConfig.portNumber)
    .withHttpApp(httpApp)
    .build

  def run(args: List[String]): IO[ExitCode] =
    server
      .useForever
      .as(ExitCode.Success)
}
