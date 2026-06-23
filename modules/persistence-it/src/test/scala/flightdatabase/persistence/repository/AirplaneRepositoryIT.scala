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
import flightdatabase.InvalidField
import flightdatabase.InvalidValueType
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airplane.Airplane
import flightdatabase.airplane.AirplaneCreate
import flightdatabase.airplane.AirplanePatch
import flightdatabase.persistence.itutils.RepositoryCheck
import flightdatabase.test.syntax.all._
import org.scalatest.Inspectors.forAll

final class AirplaneRepositoryIT extends RepositoryCheck {

  lazy val repo: AirplaneRepository[IO] = AirplaneRepository.make[IO].unsafeRunSync()

  val originalAirplanes: Nel[Airplane] = Nel.of(
    Airplane(1, "A380", 1, 853, 14800),
    Airplane(2, "747-8", 2, 410, 14310),
    Airplane(3, "A320neo", 1, 194, 6300),
    Airplane(4, "787-8", 2, 248, 13530)
  )
  val manufacturerToIdMap: Map[String, Long] = Map("Airbus" -> 1, "Boeing" -> 2)
  val idNotPresent: Long = 10
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L
  val invalidFieldSyntax: String = "Field with spaces"
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1

  val newAirplane: AirplaneCreate = AirplaneCreate("A350", 1, 325, 13900)
  val updatedName: String = "A350_updated"
  val patchedName: String = "A350_patched"

  "Checking if an airplane exists" should "return a valid result" in {
    def airplaneExists(id: Long): Boolean = repo.doesAirplaneExist(id).unsafeRunSync()
    airplaneExists(1) shouldBe true
    airplaneExists(idNotPresent) shouldBe false
    airplaneExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airplanes" should "return the correct detailed list" in {
    repo.getAirplanes(emptySortAndLimit).value should contain only (originalAirplanes.toList: _*)
  }

  it should "return a properly sorted list" in {
    val sortedAirplanes = repo.getAirplanes(ValidatedSortAndLimit.sortAscending("name")).value
    sortedAirplanes shouldBe originalAirplanes.sortBy(_.name)

    val sortedAirplanesDesc =
      repo.getAirplanes(ValidatedSortAndLimit.sortDescending("name")).value
    sortedAirplanesDesc shouldBe originalAirplanes.sortBy(_.name).reverse
  }

  it should "return only as many airplanes as requested" in {
    val limitedAirplanes = repo.getAirplanes(ValidatedSortAndLimit.limit(1)).value

    limitedAirplanes should have size 1
    limitedAirplanes should contain only originalAirplanes.head

    val limitedAirplanesWithOffset =
      repo.getAirplanes(ValidatedSortAndLimit.limitAndOffset(1, 1)).value

    limitedAirplanesWithOffset should have size 1
    limitedAirplanesWithOffset should contain only originalAirplanes.tail.head
  }

  it should "only return the requested fields if so required" in {
    val airplanesOnlyNames = repo.getAirplanes[String](emptySortAndLimit, "name").value
    airplanesOnlyNames should contain only (originalAirplanes.map(_.name).toList: _*)

    val airplanesOnlyCapacity = repo.getAirplanes[Int](emptySortAndLimit, "capacity").value
    airplanesOnlyCapacity should contain only (originalAirplanes.map(_.capacity).toList: _*)
  }

  it should "sort and return the requested fields if so required" in {
    val airplaneNames =
      repo.getAirplanes[String](ValidatedSortAndLimit.sortAscending("name"), "name").value
    airplaneNames shouldBe originalAirplanes.map(_.name).sorted

    val airplaneCapacitiesDesc =
      repo.getAirplanes[Int](ValidatedSortAndLimit.sortDescending("capacity"), "capacity").value
    airplaneCapacitiesDesc shouldBe originalAirplanes.map(_.capacity).sorted.reverse
  }

  it should "return an empty list if offset is too large" in {
    repo.getAirplanes(ValidatedSortAndLimit.offset(100)).error shouldBe EntryListEmpty
  }

  "Selecting an airplane by id" should "return the correct entry" in {
    forAll(originalAirplanes)(airplane => repo.getAirplane(airplane.id).value shouldBe airplane)
    repo.getAirplane(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirplane(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airplane by other fields" should "return the corresponding entries" in {
    def airplaneByName(name: String): IO[ApiResult[Nel[Airplane]]] =
      repo.getAirplanesBy("name", Nel.one(name), Operator.Equals, emptySortAndLimit)
    def airplaneByManufacturerId(id: Long): IO[ApiResult[Nel[Airplane]]] =
      repo.getAirplanesBy("manufacturer_id", Nel.one(id), Operator.Equals, emptySortAndLimit)

    val distinctManufacturerIds = originalAirplanes.map(_.manufacturerId).distinct

    forAll(originalAirplanes) { airplane =>
      airplaneByName(airplane.name).value should contain only airplane
    }

    forAll(distinctManufacturerIds) { id =>
      val expectedAirplanes = originalAirplanes.filter(_.manufacturerId == id)
      airplaneByManufacturerId(id).value should contain only (expectedAirplanes: _*)
    }

    airplaneByName(valueNotPresent).error shouldBe EntryListEmpty
    airplaneByManufacturerId(idNotPresent).error shouldBe EntryListEmpty
    airplaneByManufacturerId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  it should "sort and limit the filtered entries if so required" in {
    val airbusId = 1L
    val expected = originalAirplanes.filter(_.manufacturerId == airbusId)

    val sortedByName = repo
      .getAirplanesBy(
        "manufacturer_id",
        Nel.one(airbusId),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name")
      )
      .value
    sortedByName.toList shouldBe expected.sortBy(_.name)

    val limited = repo
      .getAirplanesBy(
        "manufacturer_id",
        Nel.one(airbusId),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1))
      )
      .value
    limited should contain only expected.minBy(_.name)
  }

  "Selecting an airplane by manufacturer name" should "return the corresponding entries" in {
    def airplaneByManufacturer(name: String): IO[ApiResult[Nel[Airplane]]] =
      repo.getAirplanesByManufacturer("name", Nel.one(name), Operator.Equals, emptySortAndLimit)

    forAll(manufacturerToIdMap) {
      case (manufacturer, id) =>
        airplaneByManufacturer(manufacturer).value should contain only (
          originalAirplanes.filter(_.manufacturerId == id): _*
        )
    }

    airplaneByManufacturer(valueNotPresent).error shouldBe EntryListEmpty
  }

  it should "sort and limit the entries filtered by manufacturer if so required" in {
    val expected = originalAirplanes.filter(_.manufacturerId == 1L) // Airbus

    val sortedByName = repo
      .getAirplanesByManufacturer(
        "name",
        Nel.one("Airbus"),
        Operator.Equals,
        ValidatedSortAndLimit.sortDescending("name")
      )
      .value
    sortedByName.toList shouldBe expected.sortBy(_.name).reverse

    val limited = repo
      .getAirplanesByManufacturer(
        "name",
        Nel.one("Airbus"),
        Operator.Equals,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1))
      )
      .value
    limited should contain only expected.minBy(_.name)
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getAirplanesBy(invalidFieldSyntax, Nel.one("value"), Operator.Equals, emptySortAndLimit)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirplanesByManufacturer(
        invalidFieldSyntax,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirplanesBy(invalidFieldColumn, Nel.one("value"), Operator.Equals, emptySortAndLimit)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirplanesByManufacturer(
        invalidFieldColumn,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getAirplanesBy(
        "manufacturer_id",
        Nel.one(invalidLongValue),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getAirplanesByManufacturer(
        "name",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
  }

  "Creating an airplane" should "work and return the new ID" in {
    val newId = repo.createAirplane(newAirplane).value
    val updatedAirplane = repo.getAirplane(newId).value

    newId shouldBe originalAirplanes.length + 1
    updatedAirplane shouldBe Airplane.fromCreate(newId, newAirplane)
  }

  it should "throw a conflict error if we try to create the same airplane again" in {
    repo.createAirplane(newAirplane).error shouldBe EntryAlreadyExists
  }

  it should "not allow creating a city with an empty name" in {
    val newAirplaneWithEmptyName = newAirplane.copy(name = "")
    repo
      .createAirplane(newAirplaneWithEmptyName)
      .error shouldBe EntryCheckFailed
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val newAirplaneWithNonExistingManufacturerId =
      newAirplane.copy(name = "Something", manufacturerId = idNotPresent)
    repo
      .createAirplane(newAirplaneWithNonExistingManufacturerId)
      .error shouldBe EntryHasInvalidForeignKey
  }

  it should "throw a check error if we pass negative values for capacity or maxRangeInKm" in {
    val newAirplaneWithNegativeCapacity = newAirplane.copy(capacity = -1)
    repo
      .createAirplane(newAirplaneWithNegativeCapacity)
      .error shouldBe EntryCheckFailed

    val newAirplaneWithNegativeMaxRange = newAirplane.copy(maxRangeInKm = -1)
    repo
      .createAirplane(newAirplaneWithNegativeMaxRange)
      .error shouldBe EntryCheckFailed
  }

  "Updating an airplane" should "work and return the updated airplane ID" in {
    val original = Airplane.fromCreate(originalAirplanes.length + 1, newAirplane)
    val updated = original.copy(capacity = original.capacity + 100)
    val returned = repo.updateAirplane(updated).value
    returned shouldBe updated.id
  }

  it should "also allow changing the name field" in {
    val original = Airplane.fromCreate(originalAirplanes.length + 1, newAirplane)
    val updated = original.copy(name = s"${original.name}_updated")
    val returned = repo.updateAirplane(updated).value
    returned shouldBe updated.id
  }

  it should "throw an error if we update a non-existing airplane" in {
    val updated = Airplane.fromCreate(idNotPresent, newAirplane)
    repo.updateAirplane(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val updated = Airplane.fromCreate(originalAirplanes.length + 1, newAirplane)
    val updatedWithNonExistingManufacturerId = updated.copy(manufacturerId = idNotPresent)
    repo
      .updateAirplane(updatedWithNonExistingManufacturerId)
      .error shouldBe EntryHasInvalidForeignKey
  }

  "Patching an airplane" should "work and return the patched airplane" in {
    val original = Airplane.fromCreate(originalAirplanes.length + 1, newAirplane)
    val patch = AirplanePatch(name = Some(s"${original.name}_patched"))
    val patched = Airplane.fromPatch(original.id, patch, original)
    val returned = repo.partiallyUpdateAirplane(original.id, patch).value
    returned shouldBe patched
  }

  it should "throw an error if we patch a non-existing airplane" in {
    val patch = AirplanePatch(name = Some("Something"))
    repo
      .partiallyUpdateAirplane(idNotPresent, patch)
      .error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val original = Airplane.fromCreate(originalAirplanes.length + 1, newAirplane)
    val patch = AirplanePatch(manufacturerId = Some(idNotPresent))
    repo
      .partiallyUpdateAirplane(original.id, patch)
      .error shouldBe EntryHasInvalidForeignKey
  }

  "Removing an airplane" should "work correctly" in {
    val id = originalAirplanes.length + 1
    repo.removeAirplane(id).value shouldBe ()
    repo.doesAirplaneExist(id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing airplane" in {
    repo.removeAirplane(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
