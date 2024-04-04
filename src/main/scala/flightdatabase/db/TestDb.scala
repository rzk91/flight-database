package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration
import flightdatabase.domain.FlightDbTable._
import flightdatabase.repository._
import flightdatabase.utils.TableValue

object TestDb extends IOApp.Simple with LazyLogging {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      _    <- Database.initialise[IO](conf.dbConfig, conf.cleanDatabase)
      xa   <- Database.transactor[IO](conf.dbConfig)
    } yield for {
      countries    <- getNameList(COUNTRY).transact(xa)
      _            <- IO(logger.info(s"Countries: $countries"))
      germanCities <- getNameList(CITY, Some(TableValue(COUNTRY, "Germany"))).transact(xa)
      _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
      indianCities <- getNameList(CITY, Some(TableValue(COUNTRY, "India"))).transact(xa)
      _            <- IO(logger.info(s"Cities in India: $indianCities"))
    } yield ()
  }.useEval
}
