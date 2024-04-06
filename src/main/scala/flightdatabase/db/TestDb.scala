package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository._
import flightdatabase.utils.TableValue

object TestDb extends IOApp.Simple with LazyLogging {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      db   <- Database.resource[IO](conf.dbConfig, conf.cleanDatabase)
      _    <- db.initialise()
      xa   <- db.hikariTransactor
    } yield for {
      countries <- getNameList[CountryModel].transact(xa)
      _         <- IO(logger.info(s"Countries: $countries"))
      germanCities <- getNameList[CityModel, CountryModel, String](Some(TableValue("Germany")))
        .transact(xa)
      _ <- IO(logger.info(s"Cities in Germany: $germanCities"))
      indianCities <- getNameList[CityModel, CountryModel, String](Some(TableValue("India")))
        .transact(xa)
      _ <- IO(logger.info(s"Cities in India: $indianCities"))
    } yield ()
  }.useEval
}
