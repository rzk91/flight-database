package flightdatabase.persistence.itutils

import cats.effect.Async
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.typelevel.doobie.util.transactor.Transactor
import flightdatabase.persistence.config.DatabaseConfig
import flightdatabase.persistence.db.Database
import org.scalatest.flatspec.AnyFlatSpec
import org.testcontainers.utility.DockerImageName

trait PostgreSqlContainerSpec[F[_]] extends AnyFlatSpec with ForAllTestContainer {
  implicit def async: Async[F]

  private val defaultTestConfig: DatabaseConfig = DatabaseConfig.loadUnsafe

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = DockerImageName.parse("postgres:16"),
    databaseName = defaultTestConfig.dbName,
    username = defaultTestConfig.access.username,
    password = defaultTestConfig.access.password
  )

  final lazy val testConfig: DatabaseConfig = defaultTestConfig.copy(
    driver = container.driverClassName,
    baseUrl = s"jdbc:postgresql://${container.host}:${container.firstMappedPort}"
  )

  final lazy val transactor: Transactor[F] =
    Database.makeUnsafe[F](testConfig, clean = false).simpleTransactor
}
