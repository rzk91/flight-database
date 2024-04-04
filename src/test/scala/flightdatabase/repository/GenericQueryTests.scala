package flightdatabase.repository

import cats.effect._
import doobie._
import doobie.scalatest._
import flightdatabase.config.Configuration
import flightdatabase.db.Database
import flightdatabase.domain.FlightDbTable._
import flightdatabase.repository.queries.selectFragment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GenericQueryTests extends AnyFlatSpec with Matchers with IOChecker {

  val config: Configuration.Config = Configuration.configUnsafe

  val transactor: Transactor[IO] = Database.simpleTransactor(config.dbConfig, config.cleanDatabase)

  "A simple get cities query" should "not fail" in {
    check(selectFragment("name", CITY).query[String])
  }

  "A simple get countries query" should "not fail" in {
    check(selectFragment("name", COUNTRY).query[String])
  }
}
