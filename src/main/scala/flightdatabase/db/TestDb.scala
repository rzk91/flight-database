package flightdatabase.db

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import flightdatabase.config.Configuration.{cleanDatabase, dbConfig}
import flightdatabase.db.DbMethods._
import flightdatabase.db.transactor

object TestDb extends IOApp.Simple with LazyLogging {

  override def run: IO[Unit] = {
    for {
      _  <- DbInitiation.databaseInitialisation[IO](dbConfig, cleanDatabase)
      xa <- transactor[IO]
    } yield for {
      countries    <- getStringList("country").transact(xa)
      _            <- IO(logger.info(s"Countries: $countries"))
      germanCities <- getStringListBy("city", "country", Some("Germany")).transact(xa)
      _            <- IO(logger.info(s"Cities in Germany: $germanCities"))
      indianCities <- getStringListBy("city", "country", Some("India")).transact(xa)
      _            <- IO(logger.info(s"Cities in India: $indianCities"))
    } yield ()
  }.useEval
}
