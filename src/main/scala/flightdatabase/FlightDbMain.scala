package flightdatabase

import cats.effect._
import doobie.Transactor
import flightdatabase.api.FlightDbApi
import flightdatabase.config.Configuration
import flightdatabase.db.Database
import flightdatabase.repository._

object FlightDbMain extends IOApp.Simple {

  override def run: IO[Unit] = {
    for {
      conf                          <- Configuration.configAsResource[IO]
      db                            <- Database.resource[IO](conf.dbConfig, conf.cleanDatabase)
      _                             <- db.initialise()
      implicit0(xa: Transactor[IO]) <- db.hikariTransactor
      port                          <- Resource.eval(IO.fromEither(conf.apiConfig.portNumber))
      repos                         <- RepositoryContainer.resource[IO]
      httpApp                       <- Resource.eval(FlightDbApi[IO](conf.apiConfig, repos).flightDbApp())
      _                             <- Server.start(conf.apiConfig.hostName, port, httpApp)
    } yield ()
  }.useForever
}
