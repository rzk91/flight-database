package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryCreate
import flightdatabase.domain.country.CountryPatch
import flightdatabase.testutils.RepositoryCheck
import org.scalatest.Inspectors.forAll

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

  val languageIdMap: Map[Long, (String, String)] = Map(
    1L -> ("EN", "English"),
    2L -> ("DE", "German"),
    3L -> ("TA", "Tamil"),
    4L -> ("SV", "Swedish"),
    5L -> ("AR", "Arabic"),
    6L -> ("NL", "Dutch"),
    7L -> ("HI", "Hindi")
  )

  val currencyIdMap: Map[Long, (String, String)] = Map(
    1L -> ("INR", "Indian Rupee"),
    2L -> ("EUR", "Euro"),
    3L -> ("SEK", "Swedish Krona"),
    4L -> ("AED", "Dirham"),
    5L -> ("USD", "US Dollar")
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

    forAll(originalCountries)(c => countryById(c.id).value.value shouldBe c)
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

    val distinctLanguageIds = originalCountries.flatMap(allLanguageIds).distinct

    val distinctCurrencyIds = originalCountries.map(_.currencyId).distinct

    forAll(originalCountries) { c =>
      countryByName(c.name).value.value should contain only c
      countryByIso2(c.iso2).value.value should contain only c
      countryByNationality(c.nationality).value.value should contain only c
    }

    forAll(distinctLanguageIds) { id =>
      val expectedCountries = originalCountries.filter(allLanguageIds(_).contains(id))
      countryByLanguage(id).value.value should contain only (expectedCountries: _*)
    }

    forAll(distinctCurrencyIds) { id =>
      val expectedCountries = originalCountries.filter(_.currencyId == id)
      countryByCurrency(id).value.value should contain only (expectedCountries: _*)
    }

    countryByName(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByIso2(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByNationality(valueNotPresent).left.value shouldBe EntryListEmpty
    countryByLanguage(idNotPresent).left.value shouldBe EntryListEmpty
    countryByCurrency(idNotPresent).left.value shouldBe EntryListEmpty
  }

  "Selecting a country by external fields" should "return the corresponding entries" in {
    def countriesByLanguageIso2(iso2: String): ApiResult[List[Country]] =
      repo.getCountriesByLanguage("iso2", iso2).unsafeRunSync()

    def countriesByLanguageName(name: String): ApiResult[List[Country]] =
      repo.getCountriesByLanguage("name", name).unsafeRunSync()

    def countriesByCurrencyIso(iso: String): ApiResult[List[Country]] =
      repo.getCountriesByCurrency("iso", iso).unsafeRunSync()

    def countriesByCurrencyName(name: String): ApiResult[List[Country]] =
      repo.getCountriesByCurrency("name", name).unsafeRunSync()

    forAll(languageIdMap) {
      case (id, (iso2, name)) =>
        val expectedCountries = originalCountries.filter(allLanguageIds(_).contains(id))
        countriesByLanguageIso2(iso2).value.value should contain only (expectedCountries: _*)
        countriesByLanguageName(name).value.value should contain only (expectedCountries: _*)
    }

    forAll(currencyIdMap) {
      case (id, (iso, name)) =>
        countriesByCurrencyIso(iso).value.value should contain only (
          originalCountries.filter(_.currencyId == id): _*
        )
        countriesByCurrencyName(name).value.value should contain only (
          originalCountries.filter(_.currencyId == id): _*
        )
    }
  }

  "Creating a new country" should "not take place if fields do not satisfy their criteria" in {
    val invalidCountries = List(
      newCountry.copy(name = ""),
      newCountry.copy(iso2 = ""),
      newCountry.copy(iso3 = ""),
      newCountry.copy(countryCode = 0),
      newCountry.copy(nationality = "")
    )

    forAll(invalidCountries) { c =>
      repo.createCountry(c).unsafeRunSync().left.value shouldBe EntryCheckFailed
    }
  }

  it should "not take place for a country with an existing country-unique fields" in {
    val existingCountry = originalCountries.head

    val duplicateCountries = List(
      newCountry.copy(name = existingCountry.name),
      newCountry.copy(iso2 = existingCountry.iso2),
      newCountry.copy(iso3 = existingCountry.iso3),
      newCountry.copy(countryCode = existingCountry.countryCode),
      newCountry.copy(domainName = existingCountry.domainName),
      newCountry.copy(nationality = existingCountry.nationality)
    )

    forAll(duplicateCountries) { c =>
      repo.createCountry(c).unsafeRunSync().left.value shouldBe EntryAlreadyExists
    }
  }

  it should "throw a foreign key constraint violation if a language/currency does not exist" in {
    val invalidCountries = List(
      newCountry.copy(mainLanguageId = idNotPresent),
      newCountry.copy(secondaryLanguageId = Some(idNotPresent)),
      newCountry.copy(tertiaryLanguageId = Some(idNotPresent)),
      newCountry.copy(currencyId = idNotPresent)
    )

    forAll(invalidCountries) { c =>
      repo.createCountry(c).unsafeRunSync().left.value shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "create a new country if all criteria are satisfied" in {
    val newCountryId = repo.createCountry(newCountry).unsafeRunSync().value.value
    val newCountryFromDb = repo.getCountry(newCountryId).unsafeRunSync().value.value

    newCountryFromDb shouldBe Country.fromCreate(newCountryId, newCountry)
  }

  it should "throw a conflict error if we create the same country again" in {
    repo.createCountry(newCountry).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }

  "Updating a country" should "not take place if fields do not satisfy their criteria" in {
    val existingCountry =
      repo.getCountries("name", newCountry.name).unsafeRunSync().value.value.head

    val invalidCountries = List(
      existingCountry.copy(name = ""),
      existingCountry.copy(iso2 = ""),
      existingCountry.copy(iso3 = ""),
      existingCountry.copy(countryCode = 0),
      existingCountry.copy(nationality = "")
    )

    forAll(invalidCountries) { c =>
      repo.updateCountry(c).unsafeRunSync().left.value shouldBe EntryCheckFailed
    }
  }

  it should "throw an error if we update a non-existing country" in {
    val updated = Country.fromCreate(idNotPresent, newCountry)
    repo.updateCountry(updated).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw an error if a language/currency does not exist" in {
    val existingCountry =
      repo.getCountries("name", newCountry.name).unsafeRunSync().value.value.head

    val invalidCountries = List(
      existingCountry.copy(mainLanguageId = idNotPresent),
      existingCountry.copy(secondaryLanguageId = Some(idNotPresent)),
      existingCountry.copy(tertiaryLanguageId = Some(idNotPresent)),
      existingCountry.copy(currencyId = idNotPresent)
    )

    forAll(invalidCountries) { c =>
      repo.updateCountry(c).unsafeRunSync().left.value shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "update a country if all criteria are satisfied" in {
    val existingCountry =
      repo.getCountries("name", newCountry.name).unsafeRunSync().value.value.head

    val updated = existingCountry.copy(name = updatedName)
    repo.updateCountry(updated).unsafeRunSync().value.value shouldBe existingCountry.id

    val updatedCountry = repo.getCountry(existingCountry.id).unsafeRunSync().value.value
    updatedCountry shouldBe updated
  }

  "Patching a country" should "not take place if fields do not satisfy their criteria" in {
    val existingCountry =
      repo.getCountries("name", updatedName).unsafeRunSync().value.value.head

    val invalidPatches = List(
      CountryPatch(name = Some("")),
      CountryPatch(iso2 = Some("")),
      CountryPatch(iso3 = Some("")),
      CountryPatch(countryCode = Some(0)),
      CountryPatch(nationality = Some(""))
    )

    forAll(invalidPatches) { p =>
      repo
        .partiallyUpdateCountry(existingCountry.id, p)
        .unsafeRunSync()
        .left
        .value shouldBe EntryCheckFailed
    }
  }

  it should "throw an error if we patch a non-existing country" in {
    val patched = CountryPatch(name = Some(patchedName))
    repo
      .partiallyUpdateCountry(idNotPresent, patched)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw an error if a language/currency does not exist" in {
    val existingCountry =
      repo.getCountries("name", updatedName).unsafeRunSync().value.value.head

    val invalidPatches = List(
      CountryPatch(mainLanguageId = Some(idNotPresent)),
      CountryPatch(secondaryLanguageId = Some(idNotPresent)),
      CountryPatch(tertiaryLanguageId = Some(idNotPresent)),
      CountryPatch(currencyId = Some(idNotPresent))
    )

    forAll(invalidPatches) { p =>
      repo
        .partiallyUpdateCountry(existingCountry.id, p)
        .unsafeRunSync()
        .left
        .value shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "patch a country if all criteria are satisfied" in {
    val existingCountry =
      repo.getCountries("name", updatedName).unsafeRunSync().value.value.head

    val patched = CountryPatch(name = Some(patchedName))
    repo
      .partiallyUpdateCountry(existingCountry.id, patched)
      .unsafeRunSync()
      .value
      .value shouldBe existingCountry.copy(name = patchedName)

    val patchedCountry = repo.getCountry(existingCountry.id).unsafeRunSync().value.value
    patchedCountry shouldBe existingCountry.copy(name = patchedName)
  }

  "Removing a country" should "work correctly" in {
    val existingCountry =
      repo.getCountries("name", patchedName).unsafeRunSync().value.value.head

    repo.removeCountry(existingCountry.id).unsafeRunSync().value.value shouldBe ()
    repo
      .getCountry(existingCountry.id)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(existingCountry.id)
  }

  it should "throw an error if we remove a non-existing country" in {
    repo.removeCountry(idNotPresent).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  private def allLanguageIds: Country => List[Long] =
    c => List(Some(c.mainLanguageId), c.secondaryLanguageId, c.tertiaryLanguageId).flatten
}
