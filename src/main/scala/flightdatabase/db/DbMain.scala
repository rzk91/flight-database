package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.db.DbMethods._
import flightdatabase.config.Configuration.dbConfig

object DbMain extends IOApp with LazyLogging {

  def run(args: List[String]): IO[ExitCode] = {
    DbInitiation.initializeDatabaseSeparately(dbConfig)

    transactor.use { t =>
      for {
        countries    <- getCountryNames.transact(t)
        germanCities <- getCityNames(Some("Germany")).transact(t)
        indianCities <- getCityNames(Some("India")).transact(t)
        _            <- IO(logger.info(s"Countries: $countries"))
        _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
        _            <- IO(logger.info(s"Cities in India: $indianCities"))
      } yield ExitCode.Success
    }
  }
}
