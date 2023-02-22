package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.db.DbInitiation
import flightdatabase.config.Configuration.setupConfig

import flightdatabase.db.DbMethods._
import flightdatabase.db.JsonToSqlConverter

object DbMain extends IOApp with LazyLogging {

  def run(args: List[String]): IO[ExitCode] = {
    if (setupConfig.createScripts) JsonToSqlConverter.setupScripts()

    xa.use { t =>
      for {
        _            <- DbInitiation.initialize(t)
        countries    <- getCountryNames.transact(t)
        germanCities <- getCitiesFromCountry("Germany").transact(t)
        indianCities <- getCitiesFromCountry("India").transact(t)
        _            <- IO(logger.info(s"Countries: $countries"))
        _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
        _            <- IO(logger.info(s"Cities in India: $indianCities"))
      } yield ExitCode.Success
    }
  }
}
