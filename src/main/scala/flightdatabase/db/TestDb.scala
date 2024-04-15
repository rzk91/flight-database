package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository._
import flightdatabase.utils.FieldValue

object TestDb extends IOApp.Simple with LazyLogging {

  override def run: IO[Unit] = {
    for {
      conf <- Configuration.configAsResource[IO]
      db   <- Database.resource[IO](conf.dbConfig, conf.cleanDatabase)
      _    <- db.initialise()
      xa   <- db.hikariTransactor
    } yield for {
      countries <- getFieldList[CountryModel, String]("name").transact(xa)
      _         <- IO(logger.info(s"Countries: $countries"))
      germanCities <- getFieldList[CityModel, String, CountryModel, String](
        "name",
        FieldValue("name", "Germany")
      ).transact(xa)
      _ <- IO(logger.info(s"Cities in Germany: $germanCities"))
      indianCities <- getFieldList[CityModel, String, CountryModel, String](
        "name",
        FieldValue("name", "India")
      ).transact(xa)
      _ <- IO(logger.info(s"Cities in India: $indianCities"))
      brazilianCities <- getFieldList[CityModel, String, CountryModel, String](
        "name",
        FieldValue("name", "Brazil")
      ).transact(xa)
      _ <- IO(logger.info(s"Cities in Brazil: $brazilianCities"))
    } yield ()
  }.useEval
}
