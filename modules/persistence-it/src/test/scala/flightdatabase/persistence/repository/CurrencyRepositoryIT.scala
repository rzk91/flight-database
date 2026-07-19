package flightdatabase.persistence.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.ApiResult
import flightdatabase.EntryAlreadyExists
import flightdatabase.EntryCheckFailed
import flightdatabase.EntryListEmpty
import flightdatabase.EntryNotFound
import flightdatabase.EntryValueTooLong
import flightdatabase.IntType
import flightdatabase.InvalidField
import flightdatabase.InvalidValueType
import flightdatabase.Operator
import flightdatabase.StringType
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyCreate
import flightdatabase.currency.CurrencyPatch
import flightdatabase.persistence.itutils.RepositoryCheck
import flightdatabase.test.fixtures
import flightdatabase.test.syntax.all._
import org.scalatest.Inspectors.forAll

final class CurrencyRepositoryIT extends RepositoryCheck {

  lazy val repo: CurrencyRepository[IO] = CurrencyRepository.make[IO].unsafeRunSync()

  val originalCurrencies: Nel[Currency] = fixtures.currencies

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1000000000000000000L

  val newCurrency: CurrencyCreate = CurrencyCreate("New Currency", "NCR", Some("NCR"))
  val updatedName: String = "Updated Currency"
  val patchedName: String = "Patched Currency"
  val invalidFieldSyntax: String = "Field with spaces"
  val invalidFieldColumn: String = "non_existent_field"
  val invalidStringValue: Int = 1

  "Checking if a currency exists" should "return a valid result" in {
    def currencyExists(id: Long): Boolean = repo.doesCurrencyExist(id).unsafeRunSync()
    currencyExists(1) shouldBe true
    currencyExists(idNotPresent) shouldBe false
    currencyExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all currencies" should "return the correct detailed list" in {
    repo.getCurrencies(emptySortAndLimit).value should contain only (originalCurrencies.toList: _*)
  }

  it should "return a properly sorted list" in {
    val sortedCurrencies = repo.getCurrencies(ValidatedSortAndLimit.sortAscending("name")).value
    sortedCurrencies shouldBe originalCurrencies.sortBy(_.name)

    val sortedCurrenciesDesc =
      repo.getCurrencies(ValidatedSortAndLimit.sortDescending("name")).value
    sortedCurrenciesDesc shouldBe originalCurrencies.sortBy(_.name).reverse
  }

  it should "return only as many currencies as requested" in {
    val limitedCurrencies = repo.getCurrencies(ValidatedSortAndLimit.limit(1)).value

    limitedCurrencies should have size 1
    limitedCurrencies should contain only originalCurrencies.head

    val limitedCurrenciesWithOffset =
      repo.getCurrencies(ValidatedSortAndLimit.limitAndOffset(1, 1)).value

    limitedCurrenciesWithOffset should have size 1
    limitedCurrenciesWithOffset should contain only originalCurrencies.tail.head
  }

  it should "only return the requested fields if so required" in {
    repo.getCurrencies[String](emptySortAndLimit, "name", StringType).value should contain only (
      originalCurrencies.map(_.name).toList: _*
    )

    repo.getCurrencies[String](emptySortAndLimit, "iso", StringType).value should contain only (
      originalCurrencies.map(_.iso).toList: _*
    )
  }

  it should "sort and return the requested fields if so required" in {
    val currencyNames =
      repo
        .getCurrencies[String](ValidatedSortAndLimit.sortAscending("name"), "name", StringType)
        .value
    currencyNames shouldBe originalCurrencies.map(_.name).sorted

    val currencyIsosDesc =
      repo
        .getCurrencies[String](ValidatedSortAndLimit.sortDescending("iso"), "iso", StringType)
        .value
    currencyIsosDesc shouldBe originalCurrencies.map(_.iso).sorted.reverse
  }

  it should "return an empty list if offset is too large" in {
    repo.getCurrencies(ValidatedSortAndLimit.offset(100)).error shouldBe EntryListEmpty
  }

  "Selecting a currency by ID" should "return the correct currency" in {
    forAll(originalCurrencies)(currency => repo.getCurrency(currency.id).value shouldBe currency)
    repo.getCurrency(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getCurrency(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a currency by other fields" should "return the corresponding entries" in {
    def currencyByName(name: String): IO[ApiResult[Nel[Currency]]] =
      repo.getCurrenciesBy("name", Nel.one(name), Operator.Equals, emptySortAndLimit, StringType)
    def currencyByIso(iso: String): IO[ApiResult[Nel[Currency]]] =
      repo.getCurrenciesBy("iso", Nel.one(iso), Operator.Equals, emptySortAndLimit, StringType)

    forAll(originalCurrencies) { currency =>
      currencyByName(currency.name).value should contain only currency
      currencyByIso(currency.iso).value should contain only currency
    }

    currencyByName(valueNotPresent).error shouldBe EntryListEmpty
    currencyByIso(valueNotPresent).error shouldBe EntryListEmpty
  }

  it should "sort and limit the filtered entries if so required" in {
    val sortedByName = repo
      .getCurrenciesBy(
        "symbol",
        Nel.of("₹", "€", "kr", "$"),
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name"),
        StringType
      )
      .value
    sortedByName.toList shouldBe originalCurrencies.filter(_.symbol.isDefined).sortBy(_.name)

    val limited = repo
      .getCurrenciesBy(
        "symbol",
        Nel.of("₹", "€", "kr", "$"),
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1)),
        StringType
      )
      .value
    limited should contain only originalCurrencies.filter(_.symbol.isDefined).minBy(_.name)
  }

  "Selecting a non-existent field" should "return an error" in {
    val nelValue = Nel.one("value")

    repo
      .getCurrenciesBy(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getCurrenciesBy(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getCurrenciesBy(
        "iso",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(
      invalidStringValue.toString
    )
  }

  "Creating a new currency" should "not take place if fields do not satisfy their criteria" in {
    val invalidCurrencies = List(
      newCurrency.copy(name = ""),
      newCurrency.copy(iso = "")
    )

    forAll(invalidCurrencies) { currency =>
      repo.createCurrency(currency).error shouldBe EntryCheckFailed
    }

    val invalidSymbol = newCurrency.copy(symbol = Some("Something too long"))
    repo.createCurrency(invalidSymbol).error shouldBe EntryValueTooLong(None)
  }

  it should "not take place for a currency with existing unique fields" in {
    val existingCurrency = originalCurrencies.head

    val duplicateCurrencies = List(
      newCurrency.copy(iso = existingCurrency.iso),
      newCurrency.copy(symbol = existingCurrency.symbol)
    )

    forAll(duplicateCurrencies) { currency =>
      repo.createCurrency(currency).error shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val newCurrencyId = repo.createCurrency(newCurrency).value
    val newCurrencyFromDb = repo.getCurrency(newCurrencyId).value

    newCurrencyFromDb shouldBe Currency.fromCreate(newCurrencyId, newCurrency)
  }

  it should "throw a conflict error if we create the same currency again" in {
    repo.createCurrency(newCurrency).error shouldBe EntryAlreadyExists
  }

  "Updating a currency" should "not take place if fields do not satisfy their criteria" in {
    val existingCurrency = originalCurrencies.head

    val invalidCurrencies = List(
      existingCurrency.copy(name = ""),
      existingCurrency.copy(iso = "")
    )

    forAll(invalidCurrencies) { currency =>
      repo.updateCurrency(currency).error shouldBe EntryCheckFailed
    }

    val invalidSymbol = existingCurrency.copy(symbol = Some("Something too long"))
    repo.updateCurrency(invalidSymbol).error shouldBe EntryValueTooLong(None)
  }

  it should "not take place for a currency with existing unique fields" in {
    val existingCurrency = originalCurrencies.head

    val duplicateCurrencies = List(
      existingCurrency.copy(iso = newCurrency.iso),
      existingCurrency.copy(symbol = newCurrency.symbol)
    )

    forAll(duplicateCurrencies) { currency =>
      repo.updateCurrency(currency).error shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we update a currency that does not exist" in {
    val updated = Currency.fromCreate(idNotPresent, newCurrency)
    repo.updateCurrency(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingCurrency =
      repo
        .getCurrenciesBy(
          "name",
          Nel.one(newCurrency.name),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head
    val updated = existingCurrency.copy(name = updatedName)

    repo.updateCurrency(updated).value shouldBe existingCurrency.id

    val updatedCurrencyFromDb = repo.getCurrency(existingCurrency.id).value
    updatedCurrencyFromDb shouldBe updated
  }

  "Patching a currency" should "not take place if fields do not satisfy their criteria" in {
    val existingCurrency = originalCurrencies.head

    val invalidCurrencies = List(
      CurrencyPatch(name = Some("")),
      CurrencyPatch(iso = Some(""))
    )

    forAll(invalidCurrencies) { patch =>
      repo.partiallyUpdateCurrency(existingCurrency.id, patch).error shouldBe EntryCheckFailed
    }

    val invalidSymbol = CurrencyPatch(symbol = Some("Something too long"))
    repo
      .partiallyUpdateCurrency(existingCurrency.id, invalidSymbol)
      .error shouldBe EntryValueTooLong(None)
  }

  it should "not take place for a currency with existing unique fields" in {
    val existingCurrency = originalCurrencies.head

    val duplicateCurrencies = List(
      CurrencyPatch(iso = Some(newCurrency.iso)),
      CurrencyPatch(symbol = newCurrency.symbol)
    )

    forAll(duplicateCurrencies) { patch =>
      repo.partiallyUpdateCurrency(existingCurrency.id, patch).error shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we patch a currency that does not exist" in {
    val patched = CurrencyPatch(name = Some(patchedName))
    repo.partiallyUpdateCurrency(idNotPresent, patched).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingCurrency =
      repo
        .getCurrenciesBy(
          "name",
          Nel.one(updatedName),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head
    val patched = CurrencyPatch(name = Some(patchedName))

    repo
      .partiallyUpdateCurrency(existingCurrency.id, patched)
      .value shouldBe existingCurrency.copy(name = patchedName)

    val patchedCurrencyFromDb = repo.getCurrency(existingCurrency.id).value
    patchedCurrencyFromDb.name shouldBe patchedName
  }

  "Removing a currency" should "work correctly" in {
    val existingCurrency =
      repo
        .getCurrenciesBy(
          "name",
          Nel.one(patchedName),
          Operator.Equals,
          emptySortAndLimit,
          StringType
        )
        .value
        .head
    repo.removeCurrency(existingCurrency.id).value shouldBe ()

    repo.getCurrency(existingCurrency.id).error shouldBe EntryNotFound(existingCurrency.id)
  }

  it should "not work if the currency does not exist" in {
    repo.removeCurrency(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
