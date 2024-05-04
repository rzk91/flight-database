package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidField
import flightdatabase.domain.InvalidValueType
import flightdatabase.domain.SqlError
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyCreate
import flightdatabase.domain.currency.CurrencyPatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits.enrichIOOperation
import org.scalatest.Inspectors.forAll

final class CurrencyRepositoryIT extends RepositoryCheck {

  lazy val repo: CurrencyRepository[IO] = CurrencyRepository.make[IO].unsafeRunSync()

  val originalCurrencies: List[Currency] = List(
    Currency(1, "Indian Rupee", "INR", Some("₹")),
    Currency(2, "Euro", "EUR", Some("€")),
    Currency(3, "Swedish Krona", "SEK", Some("kr")),
    Currency(4, "Dirham", "AED", None),
    Currency(5, "US Dollar", "USD", Some("$"))
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1000000000000000000L
  val stringTooLongSqlState: String = "22001"

  val newCurrency: CurrencyCreate = CurrencyCreate("New Currency", "NCR", Some("NCR"))
  val updatedName: String = "Updated Currency"
  val patchedName: String = "Patched Currency"
  val invalidFieldSyntax: String = "Field with spaces"
  val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
  val invalidFieldColumn: String = "non_existent_field"
  val invalidStringValue: Int = 1

  "Checking if a currency exists" should "return a valid result" in {
    def currencyExists(id: Long): Boolean = repo.doesCurrencyExist(id).unsafeRunSync()
    currencyExists(1) shouldBe true
    currencyExists(idNotPresent) shouldBe false
    currencyExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all currencies" should "return the correct detailed list" in {
    val currencies = repo.getCurrencies.value

    currencies should not be empty
    currencies should contain only (originalCurrencies: _*)
  }

  it should "only return names if so required" in {
    val currencyNames = repo.getCurrenciesOnlyNames.value

    currencyNames should not be empty
    currencyNames should contain only (originalCurrencies.map(_.name): _*)
  }

  "Selecting a currency by ID" should "return the correct currency" in {
    forAll(originalCurrencies)(currency => repo.getCurrency(currency.id).value shouldBe currency)
    repo.getCurrency(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getCurrency(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a currency by other fields" should "return the corresponding entries" in {
    def currencyByName(name: String): IO[ApiResult[List[Currency]]] =
      repo.getCurrencies("name", name)
    def currencyByIso(iso: String): IO[ApiResult[List[Currency]]] = repo.getCurrencies("iso", iso)

    forAll(originalCurrencies) { currency =>
      currencyByName(currency.name).value should contain only currency
      currencyByIso(currency.iso).value should contain only currency
    }

    currencyByName(valueNotPresent).error shouldBe EntryListEmpty
    currencyByIso(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a non-existent field" should "return an error" in {
    repo.getCurrencies(invalidFieldSyntax, "value").error shouldBe sqlErrorInvalidSyntax
    repo.getCurrencies(invalidFieldColumn, "value").error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo.getCurrencies("iso", invalidStringValue).error shouldBe InvalidValueType(
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
    repo.createCurrency(invalidSymbol).error shouldBe SqlError(stringTooLongSqlState)
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
    repo.updateCurrency(invalidSymbol).error shouldBe SqlError(stringTooLongSqlState)
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
    val existingCurrency = repo.getCurrencies("name", newCurrency.name).value.head
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
      .error shouldBe SqlError(stringTooLongSqlState)
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
    val existingCurrency = repo.getCurrencies("name", updatedName).value.head
    val patched = CurrencyPatch(name = Some(patchedName))

    repo
      .partiallyUpdateCurrency(existingCurrency.id, patched)
      .value shouldBe existingCurrency.copy(name = patchedName)

    val patchedCurrencyFromDb = repo.getCurrency(existingCurrency.id).value
    patchedCurrencyFromDb.name shouldBe patchedName
  }

  "Removing a currency" should "work correctly" in {
    val existingCurrency = repo.getCurrencies("name", patchedName).value.head
    repo.removeCurrency(existingCurrency.id).value shouldBe ()

    repo.getCurrency(existingCurrency.id).error shouldBe EntryNotFound(existingCurrency.id)
  }

  it should "not work if the currency does not exist" in {
    repo.removeCurrency(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
