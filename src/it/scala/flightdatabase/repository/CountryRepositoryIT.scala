package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryCreate
import flightdatabase.testutils.RepositoryCheck

class CountryRepositoryIT extends RepositoryCheck {

  lazy val repo: CountryRepository[IO] = CountryRepository.make[IO].unsafeRunSync()

  val originalCountries: List[Country] = List(
    Country(1, "India", "IN", "IND", 91, Some(".co.in"), 7, Some(1), Some(3), 1, "Indian"),
    Country(2, "Germany", "DE", "DEU", 49, Some(".de"), 2, None, None, 2, "German"),
    Country(3, "Sweden", "SE", "SWE", 46, Some(".se"), 4, None, None, 3, "Swede"),
    Country(
      4,
      "United Arab Emirates",
      "AE",
      "ARE",
      971,
      Some(".ae"),
      5,
      Some(1),
      None,
      4,
      "Emirati"
    ),
    Country(5, "Netherlands", "NL", "NLD", 31, Some(".nl"), 6, None, None, 2, "Dutch"),
    Country(
      6,
      "United States of America",
      "US",
      "USA",
      1,
      Some(".us"),
      1,
      None,
      None,
      5,
      "US citizen"
    )
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "NotPresent"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  val languageToIdMap: Map[String, Long] = Map(
    "EN" -> 1,
    "DE" -> 2,
    "TA" -> 3,
    "SV" -> 4,
    "AR" -> 5,
    "NL" -> 6,
    "HI" -> 7
  )
  val languageIdToIsoMap: Map[Long, String] = languageToIdMap.map(_.swap)

  val currencyToIdMap: Map[String, Long] = Map(
    "INR" -> 1,
    "EUR" -> 2,
    "SEK" -> 3,
    "AED" -> 4,
    "USD" -> 5
  )

  val newCountry: CountryCreate =
    CountryCreate("NewCountry", "NC", "NCT", 123, Some(".nc"), 5, Some(1), None, 4, "NewCountryian")

  val updatedName: String = "NewCountryUpdated"
  val patchedName: String = "NewCountryPatched"

  "Checking if a country exists" should "return a valid result" in {
    def countryExists(id: Long): Boolean = repo.doesCountryExist(id).unsafeRunSync()
    countryExists(1) shouldBe true
    countryExists(idNotPresent) shouldBe false
    countryExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all countries" should "return the correct detailed list" in {
    val countries = repo.getCountries.unsafeRunSync().value.value

    countries should not be empty
    countries should contain only (originalCountries: _*)
  }

  it should "return only names if so required" in {
    val countries = repo.getCountriesOnlyNames.unsafeRunSync().value.value

    countries should not be empty
    countries should contain only (originalCountries.map(_.name): _*)
  }

  "Selecting a country by ID" should "return the correct country" in {
    def countryById(id: Long): ApiResult[Country] = repo.getCountry(id).unsafeRunSync()

    originalCountries.foreach(c => countryById(c.id).value.value shouldBe c)
    countryById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent)
    countryById(veryLongIdNotPresent).left.value shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a country by other fields" should "return the corresponding entries" in {
    def countryByName(name: String): ApiResult[List[Country]] =
      repo.getCountries("name", name).unsafeRunSync()

    def countryByIso2(iso2: String): ApiResult[List[Country]] =
      repo.getCountries("iso2", iso2).unsafeRunSync()

    def countryByNationality(nationality: String): ApiResult[List[Country]] =
      repo.getCountries("nationality", nationality).unsafeRunSync()

    def countryByLanguage(id: Long): ApiResult[List[Country]] =
      repo.getCountriesByLanguage("id", id).unsafeRunSync()

    def countryByCurrency(id: Long): ApiResult[List[Country]] =
      repo.getCountriesByCurrency("id", id).unsafeRunSync()

    val distinctLanguageIds = {
      originalCountries.flatMap(c =>
        List(Some(c.mainLanguageId), c.secondaryLanguageId, c.tertiaryLanguageId).flatten
      )
    }.distinct

    val distinctCurrencyIds = originalCountries.map(_.currencyId).distinct

    originalCountries.foreach { c =>
      countryByName(c.name).value.value should contain only c
      countryByIso2(c.iso2).value.value should contain only c
      countryByNationality(c.nationality).value.value should contain only c
    }

    distinctLanguageIds.foreach { id =>
      val expectedCountries = originalCountries.filter(c =>
        List(Some(c.mainLanguageId), c.secondaryLanguageId, c.tertiaryLanguageId).flatten
          .contains(id)
      )
      countryByLanguage(id).value.value should contain only (expectedCountries: _*)
    }

    distinctCurrencyIds.foreach { id =>
      val expectedCountries = originalCountries.filter(_.currencyId == id)
      countryByCurrency(id).value.value should contain only (expectedCountries: _*)
    }

    countryByName(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByIso2(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByNationality(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByLanguage(idNotPresent).left.value shouldBe EntryListEmpty
    countryByCurrency(idNotPresent).left.value shouldBe EntryListEmpty
  }

}
