package flightdatabase.persistence.db

import cats.effect._
import cats.syntax.applicative._
import flightdatabase.persistence.config.DatabaseConfig
import org.flywaydb.core.Flyway
import org.typelevel.doobie.hikari.HikariTransactor
import org.typelevel.doobie.util.ExecutionContexts
import org.typelevel.doobie.util.log.LogHandler
import org.typelevel.doobie.util.transactor.Transactor

/**
  *  A class that manages the database connection and migration.
  *  This class is responsible for creating a transactor for executing database operations.
  *  It also initialises the database using Flyway migration.
  *  The database can be cleaned before migration, which is useful for testing purposes.
  *  Use the companion object methods to create an appropriate instance of this class.
  *
  *  @param config    The database configuration.
  *                   This includes the driver, URL, access credentials, and other settings (e.g., logging active).
  *  @param clean     A flag that indicates whether to clean the database before migration.
  *  @param locations The Flyway migration locations to scan, in classpath notation.
  * @tparam F The effect type, which must have an instance of `Async`.
  *  */
class Database[F[_]: Async] private (
  config: DatabaseConfig,
  clean: Boolean,
  locations: List[String]
) {

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
      .locations(locations: _*)
      .dataSource(config.url, config.access.username, config.access.password)
      .load()
    if (clean) flyway.clean()
    flyway.migrate().success
  }
}

object Database {

  /**
    * The default Flyway locations used in production and local runs: the app-owned schema
    * migrations plus the seed data that ships with them (see `db/migration/schema` and
    * `db/migration/seed` under the `persistence` module's resources).
    *
    * `persistence-it` overrides this to combine the shared schema location with a test-owned
    * seed location, so integration tests no longer depend on the app's seed migrations.
    */
  val DefaultLocations: List[String] =
    List("classpath:db/migration/schema", "classpath:db/migration/seed")

  def makeUnsafe[F[_]: Async](
    config: DatabaseConfig,
    clean: Boolean,
    locations: List[String] = DefaultLocations
  ): Database[F] =
    new Database[F](config, clean, locations)

  def make[F[_]: Async](
    config: DatabaseConfig,
    clean: Boolean,
    locations: List[String] = DefaultLocations
  ): F[Database[F]] =
    makeUnsafe(config, clean, locations).pure[F]

  def resource[F[_]: Async](
    config: DatabaseConfig,
    clean: Boolean,
    locations: List[String] = DefaultLocations
  ): Resource[F, Database[F]] =
    Resource.pure(makeUnsafe(config, clean, locations))
}
