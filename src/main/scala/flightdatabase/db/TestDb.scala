package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration
import flightdatabase.db.DbMethods._
import flightdatabase.model.FlightDbTable._

object TestDb extends IOApp.Simple with LazyLogging {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      _  <- DbInitiation.databaseInitialisation[IO](conf.dbConfig, conf.cleanDatabase)
      xa <- DbInitiation.transactor[IO](conf.dbConfig)
    } yield for {
      countries    <- getStringList(COUNTRY).transact(xa)
      _            <- IO(logger.info(s"Countries: $countries"))
      germanCities <- getStringListBy(CITY, COUNTRY, Some("Germany")).transact(xa)
      _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
      indianCities <- getStringListBy(CITY, COUNTRY, Some("India")).transact(xa)
      _            <- IO(logger.info(s"Cities in India: $indianCities"))
    } yield ()
  }.useEval
}
