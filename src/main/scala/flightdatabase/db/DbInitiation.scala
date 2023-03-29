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

  def simpleTransactor[F[_]: Async](config: DatabaseConfig, clean: Boolean): Transactor[F] = {
    databaseInitialisation[F](config, clean).use_

    Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      config.access.username,
      config.access.password
    )
  }

  def databaseInitialisation[F[_]: Async](config: DatabaseConfig, clean: Boolean = false)(
    implicit F: Async[F]
  ): Resource[F, Boolean] =
    Resource.eval {
      F.delay {
        val flyway = Flyway
          .configure()
          .cleanDisabled(!clean)
          .dataSource(config.url, config.access.username, config.access.password)
          .baselineVersion(config.baseline)
          .load()
        if (clean) flyway.clean()
        flyway.migrate().success
      }
    }
}
