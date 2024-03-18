package flightdatabase

import cats.effect._
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import doobie.hikari.HikariTransactor
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation
import org.http4s.HttpApp

object FlightDbMain extends IOApp.Simple {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      _    <- DbInitiation.databaseInitialisation[IO](conf.dbConfig, conf.cleanDatabase)
      // Resource-based HikariTransactor for better connection pooling
      implicit0(xa: Resource[IO, HikariTransactor[IO]]) = DbInitiation.transactor[IO](conf.dbConfig)
      port    <- Resource.eval(IO.fromEither(conf.apiConfig.portNumber))
      httpApp <- Resource.eval(FlightDbApi[IO](conf.apiConfig).flightDbApp())
      _       <- Server.start(conf.apiConfig.hostName, port, httpApp)
    } yield ()
  }.useForever
}
