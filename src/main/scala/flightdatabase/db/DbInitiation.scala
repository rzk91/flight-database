package flightdatabase.db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway

object DbInitiation {

  def transactor[F[_]: Async](config: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](config.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.driver,
        config.url,
        config.access.username,
        config.access.password,
        ec
      )
    } yield xa

  def simpleTransactor[F[_]: Async](config: DatabaseConfig): Transactor[F] = {
    val xa = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      config.access.username,
      config.access.password
    )

    // TODO: Use the above transactor's datasource for Flyway migration?
    initializeDatabaseSeparately(config)

    xa
  }

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
