package flightdatabase.repository

import cats.effect._
import doobie._
import doobie.scalatest._
import flightdatabase.config.Configuration
import flightdatabase.db.Database
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository.queries.{selectFragment, selectWhereQuery}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GenericQueryTests extends AnyFlatSpec with Matchers with IOChecker {

  val config: Configuration.Config = Configuration.configUnsafe
  val db: Database[IO] = Database.makeUnsafe(config.dbConfig, config.cleanDatabase)

  val transactor: Transactor[IO] = db.simpleTransactor

  "A simple get query" should "not fail" in {
    check(selectFragment[CityModel]("name").query[String])
    check(selectFragment[CountryModel]("name").query[String])
  }

  "A simple get query with where clause" should "not fail" in {
    check(selectWhereQuery[CountryModel, Int, String]("id", "name", "Germany"))
    check(selectWhereQuery[CityModel, String, Int]("name", "country_id", 1))
  }
}
