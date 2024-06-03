package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.itutils.DbChecker

final class GenericQueryIT extends DbChecker {

  "A simple get query" should "not fail" in {
    check(selectFragment[City]("name").query[String])
    check(selectFragment[Country]("name").query[String])
  }

  "A simple get query with multiple fields" should "not fail" in {
    check(selectFragment[City](Nel.of("name", "country_id")).query[(String, Long)])
    check(selectFragment[Country](Nel.of("name", "country_code")).query[(String, Int)])
  }

  "A simple get query with an `equals` where clause" should "not fail" in {
    check(
      selectWhereQuery[Country, Long, String]("id", "name", Nel.one("Germany"), Operator.Equals)
    )
    check(selectWhereQuery[City, String, Long]("name", "country_id", Nel.one(1), Operator.Equals))
  }

  "A simple get query with an `in` where clause" should "not fail" in {
    check(
      selectWhereQuery[Country, Long, String](
        "id",
        "name",
        Nel.of("Germany", "France"),
        Operator.In
      )
    )
    check(selectWhereQuery[City, String, Long]("name", "country_id", Nel.of(1, 2), Operator.NotIn))
  }

  "A simple inner join query" should "not fail" in {
    val allAirplanes = fr"""
       | SELECT
       |  airplane.id, airplane.name, airplane.manufacturer_id,
       |  airplane.capacity, airplane.max_range_in_km
       | FROM airplane
     """.stripMargin
    check(
      (allAirplanes ++ innerJoinWhereFragment[Airplane, Manufacturer, String](
        "name",
        Nel.one("Airbus"),
        Operator.Equals
      )).query[Airplane]
    )
  }

  "A simple inner join query with a regex where clause" should "not fail" in {
    val allAirplanes = fr"""
       | SELECT
       |  airplane.id, airplane.name, airplane.manufacturer_id,
       |  airplane.capacity, airplane.max_range_in_km
       | FROM airplane
     """.stripMargin
    check(
      (allAirplanes ++ innerJoinWhereFragment[Airplane, Manufacturer, String](
        "name",
        Nel.one("^[aA].*$"),
        Operator.RegexMatch
      )).query[Airplane]
    )
  }

  "A simple delete query" should "not fail" in {
    check(deleteWhereId[City](1))
    check(deleteWhereId[Country](1))
  }
}
