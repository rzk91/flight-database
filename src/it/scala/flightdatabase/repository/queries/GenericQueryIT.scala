package flightdatabase.repository.queries

import doobie.implicits._
import flightdatabase.domain.airplane.AirplaneModel
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.domain.manufacturer.ManufacturerModel
import flightdatabase.testutils.DbChecker

final class GenericQueryIT extends DbChecker {

  "A simple get query" should "not fail" in {
    check(selectFragment[CityModel]("name").query[String])
    check(selectFragment[CountryModel]("name").query[String])
  }

  "A simple get query with where clause" should "not fail" in {
    check(selectWhereQuery[CountryModel, Long, String]("id", "name", "Germany"))
    check(selectWhereQuery[CityModel, String, Long]("name", "country_id", 1))
  }

  "A simple inner join query" should "not fail" in {
    val allAirplanes = fr"""
       | SELECT
       |  airplane.id, airplane.name, airplane.manufacturer_id,
       |  airplane.capacity, airplane.max_range_in_km
       | FROM airplane
     """.stripMargin
    check(
      (allAirplanes ++ innerJoinWhereFragment[AirplaneModel, ManufacturerModel, String](
        "name",
        "Airbus"
      )).query[AirplaneModel]
    )
  }

  "A simple delete query" should "not fail" in {
    check(deleteWhereId[CityModel](1))
    check(deleteWhereId[CountryModel](1))
  }
}
