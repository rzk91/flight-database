package flightdatabase

import cats.effect._
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation
import org.http4s.ember.server._

object FlightDbMain extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    DbInitiation.initializeDatabaseSeparately(dbConfig)

    val flightDbApp = FlightDbApi.flightDbApp(includeLogging = true)

    EmberServerBuilder
      .default[IO]
      .withHostOption(apiConfig.hostName)
      .withPort(apiConfig.portNumber)
      .withHttpApp(flightDbApp)
      .build
      .useForever
      .as(ExitCode.Success)
  }
}
