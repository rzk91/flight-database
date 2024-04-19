package flightdatabase.repository

import cats.effect.unsafe.implicits.global
import doobie.implicits._
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.utils.FieldValue
import flightdatabase.utils.implicits.enrichConnectionIO

final class GenericRepositoryIT extends RepositoryCheck {

  "PostgreSQL container" should "pass the smoke test" in {
    sql"SELECT 1".query[Int].unique.execute.unsafeRunSync() shouldBe 1
  }

  "Selecting all country names" should "return a correct list" in {
    val countryNames = getFieldList[Country, String]("name").execute
      .unsafeRunSync()
      .value
      .value

    countryNames should not be empty
    countryNames should contain only ("India", "Germany", "Sweden", "United Arab Emirates", "Netherlands", "United States of America")
  }

  "Selecting all city names in Germany" should "return a correct list" in {
    val cityNames =
      getFieldList[City, String, Country, String]("name", FieldValue("name", "Germany")).execute
        .unsafeRunSync()
        .value
        .value

    cityNames should not be empty
    cityNames should contain only ("Berlin", "Frankfurt am Main")
  }

  "Selecting all city names in Brazil" should "return an empty list" in {
    val fieldValueBrazil = FieldValue[Country, String]("name", "Brazil")
    getFieldList[City, String, Country, String]("name", fieldValueBrazil).execute
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(fieldValueBrazil)
  }

}
