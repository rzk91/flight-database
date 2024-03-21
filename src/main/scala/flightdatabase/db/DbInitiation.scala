package flightdatabase.db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway

object DbInitiation {

  /**
    * Creates a transactor for executing database operations.
    *
    * @param config The database configuration.
    * @tparam F The effect type, which must have an instance of `Async`.
    * @return A resource that manages the lifecycle of the transactor.
    */
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

  /**
    * Creates a simple transactor for the flight database.
    *
    * @param config The database configuration.
    * @param clean  Indicates whether to clean the database before initialization.
    * @tparam F The effect type, which must have an instance of `Async`.
    * @return The created transactor.
    */
  def simpleTransactor[F[_]: Async](config: DatabaseConfig, clean: Boolean): Transactor[F] = {
    databaseInitialisation[F](config, clean).use_

    Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      config.access.username,
      config.access.password
    )
  }

  /**
    * Initializes the database using Flyway migration.
    *
    * @param config The database configuration.
    * @param clean  Flag indicating whether to clean the database before migration.
    * @tparam F The effect type, which must have an instance of `Async`.
    * @return A resource that encapsulates the database initialization process.
    */
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
