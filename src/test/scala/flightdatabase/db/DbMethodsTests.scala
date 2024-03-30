package flightdatabase.db

import cats.effect._
import doobie._
import doobie.scalatest._
import flightdatabase.config.Configuration
import flightdatabase.model.FlightDbTable._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DbMethodsTests extends AnyFlatSpec with Matchers with IOChecker {

  val config: Configuration.Config = Configuration.configUnsafe

  val transactor: Transactor[IO] = Database.simpleTransactor(config.dbConfig, config.cleanDatabase)

  "A simple get cities query" should "not fail" in {
    check(getNamesFragment(CITY).query[String])
  }

  "A simple get countries query" should "not fail" in {
    check(getNamesFragment(COUNTRY).query[String])
  }
}
