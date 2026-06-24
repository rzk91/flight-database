package flightdatabase.persistence.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.ApiResult
import flightdatabase.EntryAlreadyExists
import flightdatabase.EntryCheckFailed
import flightdatabase.EntryHasInvalidForeignKey
import flightdatabase.EntryListEmpty
import flightdatabase.EntryNotFound
import flightdatabase.IntType
import flightdatabase.InvalidField
import flightdatabase.InvalidValueType
import flightdatabase.LongType
import flightdatabase.Operator
import flightdatabase.StringType
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.country.Country
import flightdatabase.country.CountryCreate
import flightdatabase.country.CountryPatch
import flightdatabase.persistence.itutils.RepositoryCheck
import flightdatabase.test.syntax.all._
import org.scalatest.Inspectors.forAll

final class CountryRepositoryIT extends RepositoryCheck {

  lazy val repo: CountryRepository[IO] = CountryRepository.make[IO].unsafeRunSync()

  val originalCountries: Nel[Country] = Nel.of(
    Country(1, "India", "IN", "IND", 91, Some(".in"), 7, Some(1), Some(3), 1, "Indian"),
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
  val invalidFieldSyntax: String = "Field with spaces"
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1

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
    forAll(originalCountries)(c => countryExists(c.id) shouldBe true)
    countryExists(idNotPresent) shouldBe false
    countryExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all countries" should "return the correct detailed list" in {
    repo.getCountries(emptySortAndLimit).value should contain only (originalCountries.toList: _*)
  }

  it should "return a properly sorted list" in {
    val sorted = repo.getCountries(ValidatedSortAndLimit.sortAscending("name")).value
    sorted shouldBe originalCountries.sortBy(_.name)

    val sortedDesc = repo.getCountries(ValidatedSortAndLimit.sortDescending("name")).value
    sortedDesc shouldBe originalCountries.sortBy(_.name).reverse
  }

  it should "return only as many countries as requested" in {
    val limited = repo.getCountries(ValidatedSortAndLimit.limit(1)).value

    limited should have size 1
    limited should contain only originalCountries.head

    val limitedWithOffset = repo.getCountries(ValidatedSortAndLimit.limitAndOffset(1, 1)).value

    limitedWithOffset should have size 1
    limitedWithOffset should contain only originalCountries.tail.head
  }

  it should "only return the requested fields if so required" in {
    repo
      .getCountries[String](emptySortAndLimit, "name", StringType)
      .value should contain only (originalCountries.map(_.name).toList: _*)
    repo
      .getCountries[Int](emptySortAndLimit, "country_code", IntType)
      .value should contain only (originalCountries.map(_.countryCode).toList: _*)
  }

  it should "sort and return the requested fields if so required" in {
    val names =
      repo
        .getCountries[String](ValidatedSortAndLimit.sortAscending("name"), "name", StringType)
        .value
    names shouldBe originalCountries.map(_.name).sorted
  }

  it should "return an empty list if offset is too large" in {
    repo.getCountries(ValidatedSortAndLimit.offset(100)).error shouldBe EntryListEmpty
  }

  "Selecting a country by ID" should "return the correct country" in {
    forAll(originalCountries)(c => repo.getCountry(c.id).value shouldBe c)
    repo.getCountry(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getCountry(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a country by other fields" should "return the corresponding entries" in {
    def countryByName(name: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesBy("name", Nel.one(name), Operator.Equals, emptySortAndLimit, StringType)

    def countryByIso2(iso2: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesBy("iso2", Nel.one(iso2), Operator.Equals, emptySortAndLimit, StringType)

    def countryByNationality(nationality: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesBy(
        "nationality",
        Nel.one(nationality),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def countryByLanguage(id: Long): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByLanguage("id", Nel.one(id), Operator.Equals, emptySortAndLimit, LongType)

    def countryByCurrency(id: Long): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByCurrency("id", Nel.one(id), Operator.Equals, emptySortAndLimit, LongType)

    val distinctLanguageIds = originalCountries.flatMap(allLanguageIds).distinct

    val distinctCurrencyIds = originalCountries.map(_.currencyId).distinct

    forAll(originalCountries) { c =>
      countryByName(c.name).value should contain only c
      countryByIso2(c.iso2).value should contain only c
      countryByNationality(c.nationality).value should contain only c
    }

    forAll(distinctLanguageIds) { id =>
      val expectedCountries = originalCountries.filter(allLanguageIds(_).exists(_ == id))
      countryByLanguage(id).value should contain only (expectedCountries: _*)
    }

    forAll(distinctCurrencyIds) { id =>
      val expectedCountries = originalCountries.filter(_.currencyId == id)
      countryByCurrency(id).value should contain only (expectedCountries: _*)
    }

    countryByName(valueNotPresent).error shouldBe EntryListEmpty
    countryByIso2(valueNotPresent).error shouldBe EntryListEmpty
    countryByNationality(valueNotPresent).error shouldBe EntryListEmpty
    countryByLanguage(idNotPresent).error shouldBe EntryListEmpty
    countryByCurrency(idNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a country by external fields" should "return the corresponding entries" in {
    def countriesByLanguageIso2(iso2: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByLanguage(
        "iso2",
        Nel.one(iso2),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def countriesByLanguageName(name: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByLanguage(
        "name",
        Nel.one(name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def countriesByCurrencyIso(iso: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByCurrency(
        "iso",
        Nel.one(iso),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def countriesByCurrencyName(name: String): IO[ApiResult[Nel[Country]]] =
      repo.getCountriesByCurrency(
        "name",
        Nel.one(name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    forAll(languageIdMap) {
      case (id, (iso2, name)) =>
        val expectedCountries = originalCountries.filter(allLanguageIds(_).exists(_ == id))
        countriesByLanguageIso2(iso2).value should contain only (expectedCountries: _*)
        countriesByLanguageName(name).value should contain only (expectedCountries: _*)
    }

    forAll(currencyIdMap) {
      case (id, (iso, name)) =>
        countriesByCurrencyIso(iso).value should contain only (
          originalCountries.filter(_.currencyId == id): _*
        )
        countriesByCurrencyName(name).value should contain only (
          originalCountries.filter(_.currencyId == id): _*
        )
    }
  }

  "Sorting and limiting filtered countries" should "work for the direct filter" in {
    val eurId = 2L
    val expected = originalCountries.filter(_.currencyId == eurId)

    val sorted = repo
      .getCountriesBy(
        "currency_id",
        Nel.one(eurId),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name"),
        LongType
      )
      .value
    sorted.toList shouldBe expected.sortBy(_.name)
  }

  it should "work for the language filter, which unions the three language slots" in {
    // Language id 1 (English) is referenced by India (secondary), UAE (secondary),
    // and USA (main); the union must return all three, sorted across the whole result.
    val englishId = 1L
    val expected = originalCountries.filter(allLanguageIds(_).exists(_ == englishId))

    val sortedDesc = repo
      .getCountriesByLanguage(
        "id",
        Nel.one(englishId),
        Operator.Equals,
        ValidatedSortAndLimit.sortDescending("name"),
        LongType
      )
      .value
    sortedDesc.toList shouldBe expected.sortBy(_.name).reverse

    val limited = repo
      .getCountriesByLanguage(
        "id",
        Nel.one(englishId),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1)),
        LongType
      )
      .value
    limited should contain only expected.minBy(_.name)
  }

  it should "work for the currency filter" in {
    val eurId = 2L
    val expected = originalCountries.filter(_.currencyId == eurId)

    val limited = repo
      .getCountriesByCurrency(
        "iso",
        Nel.one("EUR"),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1)),
        StringType
      )
      .value
    limited should contain only expected.minBy(_.name)
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getCountriesBy(
        invalidFieldSyntax,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getCountriesByLanguage(
        invalidFieldSyntax,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getCountriesByCurrency(
        invalidFieldSyntax,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax

    repo
      .getCountriesBy(
        invalidFieldColumn,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getCountriesByLanguage(
        invalidFieldColumn,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getCountriesByCurrency(
        invalidFieldColumn,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getCountriesBy(
        "country_code",
        Nel.one(invalidLongValue),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getCountriesByLanguage(
        "iso2",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getCountriesByCurrency(
        "iso",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
  }

  "Creating a new country" should "not take place if fields do not satisfy their criteria" in {
    val invalidCountries = List(
      newCountry.copy(name = ""),
      newCountry.copy(iso2 = ""),
      newCountry.copy(iso3 = ""),
      newCountry.copy(countryCode = 0),
      newCountry.copy(nationality = "")
    )

    forAll(invalidCountries)(c => repo.createCountry(c).error shouldBe EntryCheckFailed)
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

    forAll(duplicateCountries)(c => repo.createCountry(c).error shouldBe EntryAlreadyExists)
  }

  it should "throw a foreign key constraint violation if a language/currency does not exist" in {
    val invalidCountries = List(
      newCountry.copy(mainLanguageId = idNotPresent),
      newCountry.copy(secondaryLanguageId = Some(idNotPresent)),
      newCountry.copy(tertiaryLanguageId = Some(idNotPresent)),
      newCountry.copy(currencyId = idNotPresent)
    )

    forAll(invalidCountries)(c => repo.createCountry(c).error shouldBe EntryHasInvalidForeignKey)
  }

  it should "create a new country if all criteria are satisfied" in {
    val newCountryId = repo.createCountry(newCountry).value
    val newCountryFromDb = repo.getCountry(newCountryId).value

    newCountryFromDb shouldBe Country.fromCreate(newCountryId, newCountry)
  }

  it should "throw a conflict error if we create the same country again" in {
    repo.createCountry(newCountry).error shouldBe EntryAlreadyExists
  }

  "Updating a country" should "not take place if fields do not satisfy their criteria" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(newCountry.name),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

    val invalidCountries = List(
      existingCountry.copy(name = ""),
      existingCountry.copy(iso2 = ""),
      existingCountry.copy(iso3 = ""),
      existingCountry.copy(countryCode = 0),
      existingCountry.copy(nationality = "")
    )

    forAll(invalidCountries)(c => repo.updateCountry(c).error shouldBe EntryCheckFailed)
  }

  it should "throw an error if we update a country with an existing country-unique field" in {
    val existingCountry = originalCountries.head

    val duplicateCountries = List(
      existingCountry.copy(name = newCountry.name),
      existingCountry.copy(iso2 = newCountry.iso2),
      existingCountry.copy(iso3 = newCountry.iso3),
      existingCountry.copy(countryCode = newCountry.countryCode),
      existingCountry.copy(domainName = newCountry.domainName),
      existingCountry.copy(nationality = newCountry.nationality)
    )

    forAll(duplicateCountries)(c => repo.updateCountry(c).error shouldBe EntryAlreadyExists)
  }

  it should "throw an error if we update a non-existing country" in {
    val updated = Country.fromCreate(idNotPresent, newCountry)
    repo.updateCountry(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw an error if a language/currency does not exist" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(newCountry.name),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

    val invalidCountries = List(
      existingCountry.copy(mainLanguageId = idNotPresent),
      existingCountry.copy(secondaryLanguageId = Some(idNotPresent)),
      existingCountry.copy(tertiaryLanguageId = Some(idNotPresent)),
      existingCountry.copy(currencyId = idNotPresent)
    )

    forAll(invalidCountries)(c => repo.updateCountry(c).error shouldBe EntryHasInvalidForeignKey)
  }

  it should "update a country if all criteria are satisfied" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(newCountry.name),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

    val updated = existingCountry.copy(name = updatedName)
    repo.updateCountry(updated).value shouldBe existingCountry.id

    val updatedCountry = repo.getCountry(existingCountry.id).value
    updatedCountry shouldBe updated
  }

  "Patching a country" should "not take place if fields do not satisfy their criteria" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(updatedName),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

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
        .error shouldBe EntryCheckFailed
    }
  }

  it should "throw an error if we patch a non-existing country" in {
    val patched = CountryPatch(name = Some(patchedName))
    repo
      .partiallyUpdateCountry(idNotPresent, patched)
      .error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not take place for a country with an existing country-unique field" in {
    val existingCountry = originalCountries.head

    val duplicatePatches = List(
      CountryPatch(name = Some(updatedName)),
      CountryPatch(iso2 = Some(newCountry.iso2)),
      CountryPatch(iso3 = Some(newCountry.iso3)),
      CountryPatch(countryCode = Some(newCountry.countryCode)),
      CountryPatch(domainName = newCountry.domainName),
      CountryPatch(nationality = Some(newCountry.nationality))
    )

    forAll(duplicatePatches) { p =>
      repo
        .partiallyUpdateCountry(existingCountry.id, p)
        .error shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if a language/currency does not exist" in {
    val existingCountry = originalCountries.head

    val invalidPatches = List(
      CountryPatch(mainLanguageId = Some(idNotPresent)),
      CountryPatch(secondaryLanguageId = Some(idNotPresent)),
      CountryPatch(tertiaryLanguageId = Some(idNotPresent)),
      CountryPatch(currencyId = Some(idNotPresent))
    )

    forAll(invalidPatches) { p =>
      repo
        .partiallyUpdateCountry(existingCountry.id, p)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "patch a country if all criteria are satisfied" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(updatedName),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

    val patched = CountryPatch(name = Some(patchedName))
    repo
      .partiallyUpdateCountry(existingCountry.id, patched)
      .value shouldBe existingCountry.copy(name = patchedName)

    val patchedCountry = repo.getCountry(existingCountry.id).value
    patchedCountry shouldBe existingCountry.copy(name = patchedName)
  }

  "Removing a country" should "work correctly" in {
    val existingCountry =
      repo
        .getCountriesBy(
          "name",
          Nel.one(patchedName),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head

    repo.removeCountry(existingCountry.id).value shouldBe ()
    repo
      .getCountry(existingCountry.id)
      .error shouldBe EntryNotFound(existingCountry.id)
  }

  it should "throw an error if we remove a non-existing country" in {
    repo.removeCountry(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }

  private def allLanguageIds: Country => Nel[Long] =
    c =>
      Nel.fromListUnsafe(
        List(Some(c.mainLanguageId), c.secondaryLanguageId, c.tertiaryLanguageId).flatten
      )
}
