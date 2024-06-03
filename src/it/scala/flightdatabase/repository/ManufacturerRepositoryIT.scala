package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.Operator
import flightdatabase.domain.ApiError
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidField
import flightdatabase.domain.InvalidValueType
import flightdatabase.domain.SqlError
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerCreate
import flightdatabase.domain.manufacturer.ManufacturerPatch
import flightdatabase.itutils.RepositoryCheck
import flightdatabase.itutils.implicits._
import org.scalatest.Inspectors.forAll

final class ManufacturerRepositoryIT extends RepositoryCheck {

  lazy val repo: ManufacturerRepository[IO] = ManufacturerRepository.make[IO].unsafeRunSync()

  val originalManufacturers: Nel[Manufacturer] = Nel.of(
    Manufacturer(1, "Airbus", 5),
    Manufacturer(2, "Boeing", 6)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "NotPresent"
  val veryLongIdNotPresent: Long = 1039495454540034858L
  val invalidFieldSyntax: String = "Field with spaces"
  val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1

  val cityIdMap: Map[Long, String] = Map(5L -> "Leiden", 6L -> "Chicago")

  val cityIdCountryMap: Map[Long, String] =
    Map(5L -> "Netherlands", 6L -> "United States of America")

  val newManufacturer: ManufacturerCreate = ManufacturerCreate("ADA", 1)
  val updatedName: String = "Aeronautical Agency"
  val patchedName: String = "Aeronautical Development Agency"

  "Checking if a manufacturer exists" should "return a valid result" in {
    def manufacturerExists(id: Long): Boolean = repo.doesManufacturerExist(id).unsafeRunSync()
    forAll(originalManufacturers)(m => manufacturerExists(m.id) shouldBe true)
    manufacturerExists(idNotPresent) shouldBe false
    manufacturerExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all manufacturers" should "return the correct detailed list" in {
    repo.getManufacturers.value should contain only (originalManufacturers.toList: _*)
  }

  it should "return only names if so required" in {
    repo.getManufacturersOnly[String]("name").value should contain only (
      originalManufacturers.map(_.name).toList: _*
    )

    repo.getManufacturersOnly[Long]("base_city_id").value should contain only (
      originalManufacturers.map(_.baseCityId).toList: _*
    )
  }

  "Selecting a manufacturer by ID" should "return the correct manufacturer" in {
    forAll(originalManufacturers)(m => repo.getManufacturer(m.id).value shouldBe m)
    repo.getManufacturer(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getManufacturer(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a manufacturer by other fields" should "return the correct manufacturer(s)" in {
    def manufacturerByName(name: String): IO[ApiResult[Nel[Manufacturer]]] =
      repo.getManufacturersBy("name", Nel.one(name), Operator.Equals)

    def manufacturerByCity(cityId: Long): IO[ApiResult[Nel[Manufacturer]]] =
      repo.getManufacturersBy("base_city_id", Nel.one(cityId), Operator.Equals)

    val distinctCityIds = originalManufacturers.map(_.baseCityId).distinct

    forAll(originalManufacturers)(m => manufacturerByName(m.name).value should contain only m)
    forAll(distinctCityIds) { cityId =>
      manufacturerByCity(cityId).value should contain only (
        originalManufacturers.filter(_.baseCityId == cityId): _*
      )
    }
    manufacturerByName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a manufacturer by city/country name" should "return the correct list of manufacturers" in {
    def manufacturerByCity(city: String): IO[ApiResult[Nel[Manufacturer]]] =
      repo.getManufacturersByCity("name", Nel.one(city), Operator.Equals)

    def manufacturerByCountry(country: String): IO[ApiResult[Nel[Manufacturer]]] =
      repo.getManufacturersByCountry("name", Nel.one(country), Operator.Equals)

    forAll(cityIdMap) {
      case (cityId, cityName) =>
        manufacturerByCity(cityName).value should contain only (
          originalManufacturers.filter(_.baseCityId == cityId): _*
        )
    }

    forAll(cityIdCountryMap) {
      case (cityId, countryName) =>
        manufacturerByCountry(countryName).value should contain only (
          originalManufacturers.filter(_.baseCityId == cityId): _*
        )
    }

    manufacturerByCity(valueNotPresent).error shouldBe EntryListEmpty
    manufacturerByCountry(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getManufacturersBy(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getManufacturersByCity(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getManufacturersByCountry(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax

    repo
      .getManufacturersBy(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getManufacturersByCity(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getManufacturersByCountry(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getManufacturersBy("name", Nel.one(invalidStringValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getManufacturersBy("base_city_id", Nel.one(invalidLongValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getManufacturersByCity("population", Nel.one(invalidLongValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getManufacturersByCountry("name", Nel.one(invalidStringValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidStringValue.toString)
  }

  "Creating a new manufacturer" should "not take place if fields do not satisfy their criteria" in {
    val manufacturerWithInvalidName = newManufacturer.copy(name = "")
    repo.createManufacturer(manufacturerWithInvalidName).error shouldBe EntryCheckFailed

    val manufacturerWithNonUniqueName = newManufacturer.copy(name = originalManufacturers.head.name)
    repo.createManufacturer(manufacturerWithNonUniqueName).error shouldBe EntryAlreadyExists

    val manufacturerWithNonExistingCity = newManufacturer.copy(baseCityId = idNotPresent)
    repo
      .createManufacturer(manufacturerWithNonExistingCity)
      .error shouldBe EntryHasInvalidForeignKey
  }

  it should "take place if all fields satisfy their criteria" in {
    val newId = repo.createManufacturer(newManufacturer).value
    val newManufacturerFromDb = repo.getManufacturer(newId).value
    newManufacturerFromDb shouldBe Manufacturer.fromCreate(newId, newManufacturer)
  }

  it should "throw a conflict error if the manufacturer already exists" in {
    repo.createManufacturer(newManufacturer).error shouldBe EntryAlreadyExists
  }

  "Updating a manufacturer" should "not take place if fields do not satisfy their criteria" in {
    val existingManufacturer = originalManufacturers.head

    val manufacturerWithInvalidName = existingManufacturer.copy(name = "")
    repo.updateManufacturer(manufacturerWithInvalidName).error shouldBe EntryCheckFailed

    val manufacturerWithNonUniqueName = existingManufacturer.copy(name = newManufacturer.name)
    repo.updateManufacturer(manufacturerWithNonUniqueName).error shouldBe EntryAlreadyExists

    val manufacturerWithNonExistingCity = existingManufacturer.copy(baseCityId = idNotPresent)
    repo
      .updateManufacturer(manufacturerWithNonExistingCity)
      .error shouldBe EntryHasInvalidForeignKey
  }

  it should "not work for a non-existing manufacturer" in {
    val updated = Manufacturer.fromCreate(idNotPresent, newManufacturer)
    repo.updateManufacturer(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingManufacturer =
      repo.getManufacturersBy("name", Nel.one(newManufacturer.name), Operator.Equals).value.head

    val updated = existingManufacturer.copy(name = updatedName)
    repo.updateManufacturer(updated).value shouldBe existingManufacturer.id

    val updatedManufacturer = repo.getManufacturer(existingManufacturer.id).value
    updatedManufacturer shouldBe updated
  }

  "Patching a manufacturer" should "not take place if fields do not satisfy their criteria" in {
    val existingManufacturer = originalManufacturers.head
    def patchManufacturer(patch: ManufacturerPatch): ApiError =
      repo.partiallyUpdateManufacturer(existingManufacturer.id, patch).error

    val manufacturerWithInvalidName = ManufacturerPatch(name = Some(""))
    patchManufacturer(manufacturerWithInvalidName) shouldBe EntryCheckFailed

    val manufacturerWithNonUniqueName = ManufacturerPatch(name = Some(updatedName))
    patchManufacturer(manufacturerWithNonUniqueName) shouldBe EntryAlreadyExists

    val manufacturerWithNonExistingCity = ManufacturerPatch(baseCityId = Some(idNotPresent))
    patchManufacturer(manufacturerWithNonExistingCity) shouldBe EntryHasInvalidForeignKey
  }

  it should "not work for a non-existing manufacturer" in {
    val patched = ManufacturerPatch(name = Some(patchedName))
    repo.partiallyUpdateManufacturer(idNotPresent, patched).error shouldBe EntryNotFound(
      idNotPresent
    )
  }

  it should "work if all criteria are met" in {
    val existingManufacturer =
      repo.getManufacturersBy("name", Nel.one(updatedName), Operator.Equals).value.head

    val patch = ManufacturerPatch(name = Some(patchedName))
    val patched = existingManufacturer.copy(name = patchedName)
    repo.partiallyUpdateManufacturer(existingManufacturer.id, patch).value shouldBe patched

    repo.getManufacturer(existingManufacturer.id).value shouldBe patched
  }

  "Removing a manufacturer" should "work correctly" in {
    val existingManufacturer =
      repo.getManufacturersBy("name", Nel.one(patchedName), Operator.Equals).value.head
    repo.removeManufacturer(existingManufacturer.id).value shouldBe ()
    repo.getManufacturer(existingManufacturer.id).error shouldBe EntryNotFound(
      existingManufacturer.id
    )
  }

  it should "not work for a non-existing manufacturer" in {
    repo.removeManufacturer(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
