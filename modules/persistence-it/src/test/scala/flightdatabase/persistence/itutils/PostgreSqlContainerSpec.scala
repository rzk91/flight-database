package flightdatabase.persistence.itutils

import cats.effect.Async
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import flightdatabase.persistence.config.Access
import flightdatabase.persistence.config.DatabaseConfig
import flightdatabase.persistence.db.Database
import org.scalatest.flatspec.AnyFlatSpec
import org.testcontainers.utility.DockerImageName
import org.typelevel.doobie.util.transactor.Transactor

trait PostgreSqlContainerSpec[F[_]] extends AnyFlatSpec with ForAllTestContainer {
  implicit def async: Async[F]

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = DockerImageName.parse("postgres:16")
  )

  final lazy val testConfig: DatabaseConfig = DatabaseConfig(
    driver = container.driverClassName,
    baseUrl = s"jdbc:postgresql://${container.host}:${container.firstMappedPort}",
    dbName = container.databaseName,
    access = Access(container.username, container.password),
    baseline = "3.0",
    threadPoolSize = 32,
    cleanDatabase = false,
    loggingActive = true
  )

  final lazy val transactor: Transactor[F] =
    Database.makeUnsafe[F](testConfig, clean = false).simpleTransactor
}
