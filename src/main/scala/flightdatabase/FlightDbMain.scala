package flightdatabase

import cats.effect._
import com.comcast.ip4s.{Host, Port}
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation
import org.http4s.HttpApp

object FlightDbMain extends Server[IO] with IOApp {

  val host: Option[Host] = apiConfig.hostName
  val port: Port = apiConfig.portNumber
  val httpApp: HttpApp[IO] = FlightDbApi[IO].flightDbApp(includeLogging = true)

  def run(args: List[String]): IO[ExitCode] = {
    DbInitiation.initializeDatabaseSeparately(dbConfig)

    server.useForever.as(ExitCode.Success)
  }
}
