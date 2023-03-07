package flightdatabase.db

import cats.effect._
import doobie._
import doobie.scalatest._
import flightdatabase.config.Configuration.dbConfig
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DbMethodsTests extends AnyFlatSpec with Matchers with IOChecker {

  val transactor: Transactor[IO] = DbInitiation.simpleTransactor(dbConfig)

  "A simple get cities query" should "not fail" in {
    check(getNamesFragment("city").query[String])
  }

  "A simple get countries query" should "not fail" in {
    check(getNamesFragment("country").query[String])
  }
}
