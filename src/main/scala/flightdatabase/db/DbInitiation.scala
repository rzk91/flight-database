package flightdatabase.db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway

object DbInitiation {

  def transactor(config: DatabaseConfig): Resource[IO, HikariTransactor[IO]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](config.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.driver,
        config.url,
        config.access.username,
        config.access.password,
        ec
      )
    } yield xa

  def initializeDatabaseSeparately(config: DatabaseConfig): Boolean = {
    val flyway = Flyway
      .configure()
      .cleanDisabled(!cleanDatabase)
      .dataSource(config.url, config.access.username, config.access.password)
      .baselineVersion(config.baseline)
      .load()
    if (cleanDatabase) flyway.clean()
    flyway.migrate().success
  }
}
