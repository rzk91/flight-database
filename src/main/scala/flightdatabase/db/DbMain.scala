package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration.dbConfig
import flightdatabase.db.DbMethods._

object DbMain extends IOApp with LazyLogging {

  def run(args: List[String]): IO[ExitCode] = {
    DbInitiation.initializeDatabaseSeparately(dbConfig)

    transactor[IO].use { t =>
      for {
        countries    <- getStringList("country").transact(t)
        _            <- IO(logger.info(s"Countries: $countries"))
        germanCities <- getStringListBy("city", "country", Some("Germany")).transact(t)
        _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
        indianCities <- getStringListBy("city", "country", Some("India")).transact(t)
        _            <- IO(logger.info(s"Cities in India: $indianCities"))
      } yield ExitCode.Success
    }
  }
}
