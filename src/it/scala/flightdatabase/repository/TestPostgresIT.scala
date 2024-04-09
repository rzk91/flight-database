package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.implicits._
import doobie.util.transactor.Transactor
import flightdatabase.config.Configuration
import flightdatabase.config.Configuration.DatabaseConfig
import flightdatabase.db.Database
import flightdatabase.domain.city.CityModel
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues._
import org.testcontainers.utility.DockerImageName

class TestPostgresIT extends AnyFlatSpec with ForAllTestContainer with Matchers {

  val defaultTestConfig: DatabaseConfig = Configuration.configUnsafe.dbConfig

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = DockerImageName.parse("postgres:16"),
    databaseName = defaultTestConfig.dbName,
    username = defaultTestConfig.access.username,
    password = defaultTestConfig.access.password
  )

  lazy val testConfig: DatabaseConfig = defaultTestConfig.copy(
    driver = container.driverClassName,
    baseUrl = container.jdbcUrl.stripSuffix(s"/${defaultTestConfig.dbName}")
  )

  lazy val transactor: Transactor[IO] =
    Database.makeUnsafe[IO](testConfig, clean = false).simpleTransactor

  "PostgreSQL container" should "be able to run a simple select query" in {
    sql"SELECT 1".query[Int].unique.transact(transactor).unsafeRunSync() shouldBe 1
  }

  "PostgreSQL container" should "run a simple query against the Flight Database" in {
    val cityNames = getFieldList[CityModel, String]("name")
      .transact(transactor)
      .unsafeRunSync()
      .map(_.value)
      .value

    cityNames should not be empty
    cityNames should contain allOf ("Bangalore", "Frankfurt am Main")
  }
}
