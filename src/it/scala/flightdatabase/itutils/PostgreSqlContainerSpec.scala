package flightdatabase.itutils

import cats.effect.Async
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration
import flightdatabase.config.Configuration.DatabaseConfig
import flightdatabase.db.Database
import org.scalatest.flatspec.AnyFlatSpec
import org.testcontainers.utility.DockerImageName

trait PostgreSqlContainerSpec[F[_]] extends AnyFlatSpec with ForAllTestContainer {
  implicit def async: Async[F]

  private val defaultTestConfig: DatabaseConfig = Configuration.configUnsafe.dbConfig

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = DockerImageName.parse("postgres:16"),
    databaseName = defaultTestConfig.dbName,
    username = defaultTestConfig.access.username,
    password = defaultTestConfig.access.password
  )

  final lazy val testConfig: DatabaseConfig = defaultTestConfig.copy(
    driver = container.driverClassName,
    baseUrl = container.jdbcUrl.stripSuffix(s"/${defaultTestConfig.dbName}")
  )

  final lazy val transactor: Transactor[F] =
    Database.makeUnsafe[F](testConfig, clean = false).simpleTransactor
}
