package flightdatabase

import cats.effect._
import com.comcast.ip4s.{Host, Port}
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation
import org.http4s.HttpApp

object FlightDbMain extends Server[IO] with IOApp.Simple {

  val host: Option[Host] = apiConfig.hostName
  val port: Port = apiConfig.portNumber
  val httpApp: HttpApp[IO] = FlightDbApi[IO].flightDbApp(includeLogging = true)

  override def run: IO[Unit] = {
    for {
      _ <- DbInitiation.databaseInitialisation[IO](dbConfig, cleanDatabase)
      _ <- server
    } yield ()
  }.useForever
}
