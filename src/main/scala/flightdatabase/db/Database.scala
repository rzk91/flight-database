package flightdatabase.db

import cats.effect._
import cats.syntax.applicative._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration._
import org.flywaydb.core.Flyway

/**
  *  A class that manages the database connection and migration.
  *  This class is responsible for creating a transactor for executing database operations.
  *  It also initialises the database using Flyway migration.
  *  The database can be cleaned before migration, which is useful for testing purposes.
  *  Use the companion object methods to create an appropriate instance of this class.
  *
  *  @param config The database configuration.
  *                This includes the driver, URL, access credentials, and other settings (e.g., logging active).
  *  @param clean  A flag that indicates whether to clean the database before migration.
  * @tparam F The effect type, which must have an instance of `Async`.
  *  */
class Database[F[_]: Async] private (config: DatabaseConfig, clean: Boolean) {

  private val logHandler: Option[LogHandler[F]] =
    Option(Log4jHandler.create(getClass.getName)).filter(_ => config.loggingActive)

  /**
    * Creates a Hikari transactor for executing database operations.
    *
    * @return A resource that manages the lifecycle of the Hikari transactor.
    */
  def hikariTransactor: Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](config.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.driver,
        config.url,
        config.access.username,
        config.access.password,
        ec,
        logHandler
      )
    } yield xa

  /**
    * Creates a simple transactor for the flight database.
    *
    * Note that this function is not wrapped in any effect type.
    * It is intended to only be used in tests or for non-production use cases.
    *
    * @return The created transactor after Flyway migration.
    */
  def simpleTransactor: Transactor[F] = {
    flywayMigration()

    Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      config.access.username,
      config.access.password,
      logHandler
    )
  }

  /**
    * Initialises the database using Flyway migration.
    *
    * @return A resource that encapsulates the database initialization process.
    */
  def initialise(): Resource[F, Boolean] = Resource.pure(flywayMigration())

  /**
    * Performs Flyway migration for the database.
    *
    * @return True if the migration is successful, false otherwise.
    */
  private def flywayMigration(): Boolean = {
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

object Database {

  def makeUnsafe[F[_]: Async](config: DatabaseConfig, clean: Boolean): Database[F] =
    new Database[F](config, clean)

  def make[F[_]: Async](config: DatabaseConfig, clean: Boolean): F[Database[F]] =
    makeUnsafe(config, clean).pure[F]

  def resource[F[_]: Async](config: DatabaseConfig, clean: Boolean): Resource[F, Database[F]] =
    Resource.pure(makeUnsafe(config, clean))
}
