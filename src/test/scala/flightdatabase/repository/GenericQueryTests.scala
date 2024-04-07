package flightdatabase.repository

import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository.queries._
import flightdatabase.testutils.DbChecker

final class GenericQueryTests extends DbChecker {

  "A simple get query" should "not fail" in {
    check(selectFragment[CityModel]("name").query[String])
    check(selectFragment[CountryModel]("name").query[String])
  }

  "A simple get query with where clause" should "not fail" in {
    check(selectWhereQuery[CountryModel, Long, String]("id", "name", "Germany"))
    check(selectWhereQuery[CityModel, String, Long]("name", "country_id", 1))
  }

  "A simple delete query" should "not fail" in {
    check(deleteWhereId[CityModel](1))
    check(deleteWhereId[CountryModel](1))
  }
}
