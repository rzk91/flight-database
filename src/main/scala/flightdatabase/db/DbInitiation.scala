package flightdatabase.db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway
import scala.concurrent.ExecutionContext

object DbInitiation {

  def transactor(config: DatabaseConfig, ec: ExecutionContext): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](
        config.driver,
        config.url,
        config.access.username,
        config.access.password,
        ec
      )

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { datasource =>
      IO {
        val flyway = Flyway
          .configure()
          .dataSource(datasource)
          .baselineVersion(dbConfig.baseline)
          .load()
        if (setupConfig.cleanDatabase) flyway.clean()
        flyway.migrate()
        ()
      }
    }
}
