package flightdatabase.db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway

object DbInitiation {

  def transactor(config: DatabaseConfig): Resource[IO, HikariTransactor[IO]] = {
    val t = for {
      ec <- ExecutionContexts.fixedThreadPool[IO](config.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.driver,
        config.url,
        config.access.username,
        config.access.password,
        ec
      )
    } yield xa

    t.preAllocate(initialize(config))
  }

  private def initialize(config: DatabaseConfig): IO[Unit] =
    IO {
      val flyway = Flyway
        .configure()
        .dataSource(config.url, config.access.username, config.access.password)
        .baselineVersion(dbConfig.baseline)
        .load()
      if (config.cleanDatabase) flyway.clean()
      flyway.migrate()
      ()
    }
}
