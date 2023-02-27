package flightdatabase.api

import cats.effect._
import cats.syntax.all._
import flightdatabase.api.ApiEndpoints._
import flightdatabase.config.Configuration.apiConfig
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object ApiMain extends IOApp {

  private val services = helloWorldService <+> flightDbService
  private val httpApp = Router("/" -> helloWorldService, "/flightdb" -> services).orNotFound

  private val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

  private val server = EmberServerBuilder
    .default[IO]
    .withHostOption(apiConfig.hostName)
    .withPort(apiConfig.portNumber)
    .withHttpApp(finalHttpApp)
    .build

  def run(args: List[String]): IO[ExitCode] =
    server.useForever.as(ExitCode.Success)
}
