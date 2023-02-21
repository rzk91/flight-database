package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.ExecutionContexts
import flightdatabase.config.Configuration._
import flightdatabase.db.DbInitiation._
import flightdatabase.db.DbMethods._
import flightdatabase.db.JsonToSqlConverter

object DbMain extends IOApp with LazyLogging {

  val xa = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](dbConfig.threadPoolSize)
    xa <- transactor(dbConfig, ec)
  } yield xa

  def run(args: List[String]): IO[ExitCode] = {
    if (setupConfig.createScripts) JsonToSqlConverter.setupScripts()

    xa.use { t =>
      for {
        _            <- initialize(t)
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
