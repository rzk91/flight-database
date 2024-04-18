package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.airplane.AirplanePatch
import flightdatabase.testutils.RepositoryCheck

final class AirplaneRepositoryIT extends RepositoryCheck {

  lazy val repo: AirplaneRepository[IO] = AirplaneRepository.make[IO].unsafeRunSync()

  val originalExpectedAirplanes: List[Airplane] = List(
    Airplane(1, "A380", 1, 853, 14800),
    Airplane(2, "747-8", 2, 410, 14310),
    Airplane(3, "A320neo", 1, 194, 6300),
    Airplane(4, "787-8", 2, 248, 13530)
  )
  val manufacturerToIdMap: Map[String, Long] = Map("Airbus" -> 1, "Boeing" -> 2)
  val idNotPresent: Long = 10
  val veryLongIdNotPresent: Long = 1039495454540034858L

  val newAirplane: AirplaneCreate = AirplaneCreate("A350", 1, 325, 13900)

  "Checking if an airplane exists" should "return a valid result" in {
    def airplaneExists(id: Long): Boolean =
      repo.doesAirplaneExist(id).unsafeRunSync()
    airplaneExists(1) shouldBe true
    airplaneExists(idNotPresent) shouldBe false
    airplaneExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airplanes" should "return the correct detailed list" in {
    val airplanes = repo.getAirplanes.unsafeRunSync().value.value

    airplanes should not be empty
    airplanes should contain only (originalExpectedAirplanes: _*)
  }

  it should "return only names if so required" in {
    val airplanesOnlyNames = repo.getAirplanesOnlyNames.unsafeRunSync().value.value
    airplanesOnlyNames should not be empty
    airplanesOnlyNames should contain only (originalExpectedAirplanes.map(_.name): _*)
  }

  "Selecting an airplane by id" should "return the correct entry" in {
    def airplaneById(id: Long): ApiResult[Airplane] = repo.getAirplane(id).unsafeRunSync()

    originalExpectedAirplanes.foreach { airplane =>
      airplaneById(airplane.id).value.value shouldBe airplane
    }
    airplaneById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent)
    airplaneById(veryLongIdNotPresent).left.value shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airplane by other fields" should "return the corresponding entries" in {
    def airplaneByName(name: String): ApiResult[List[Airplane]] =
      repo.getAirplanes("name", name).unsafeRunSync()
    def airplaneByManufacturerId(id: Long): ApiResult[List[Airplane]] =
      repo.getAirplanes("manufacturer_id", id).unsafeRunSync()

    val distinctManufacturerIds = originalExpectedAirplanes.map(_.manufacturerId).distinct

    originalExpectedAirplanes.foreach { airplane =>
      airplaneByName(airplane.name).value.value should contain only airplane
    }

    distinctManufacturerIds.foreach { id =>
      val expectedAirplanes = originalExpectedAirplanes.filter(_.manufacturerId == id)
      airplaneByManufacturerId(id).value.value should contain only (expectedAirplanes: _*)
    }

    airplaneByName("Not present").left.value shouldBe EntryListEmpty
    airplaneByManufacturerId(idNotPresent).left.value shouldBe EntryListEmpty
    airplaneByManufacturerId(veryLongIdNotPresent).left.value shouldBe EntryListEmpty
  }

  "Selecting an airplane by manufacturer name" should "return the corresponding entries" in {
    def airplaneByManufacturer(name: String): ApiResult[List[Airplane]] =
      repo.getAirplanesByManufacturer(name).unsafeRunSync()

    manufacturerToIdMap.foreach {
      case (manufacturer, id) =>
        airplaneByManufacturer(manufacturer).value.value should contain only (
          originalExpectedAirplanes.filter(_.manufacturerId == id): _*
        )
    }

    airplaneByManufacturer("Not present").left.value shouldBe EntryListEmpty
  }

  "Creating an airplane" should "work and return the new ID" in {
    val newId = repo.createAirplane(newAirplane).unsafeRunSync().value.value
    val updatedAirplanes = repo.getAirplanes.unsafeRunSync().value.value

    newId shouldBe originalExpectedAirplanes.length + 1
    updatedAirplanes should contain(Airplane.fromCreate(newId, newAirplane))
  }

  it should "throw a conflict error if we try to create the same airplane again" in {
    repo.createAirplane(newAirplane).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val newAirplaneWithNonExistingManufacturerId =
      newAirplane.copy(name = "Something", manufacturerId = idNotPresent)
    repo
      .createAirplane(newAirplaneWithNonExistingManufacturerId)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  it should "throw a check error if we pass negative values for capacity or maxRangeInKm" in {
    val newAirplaneWithNegativeCapacity = newAirplane.copy(capacity = -1)
    repo
      .createAirplane(newAirplaneWithNegativeCapacity)
      .unsafeRunSync()
      .left
      .value shouldBe EntryCheckFailed

    val newAirplaneWithNegativeMaxRange = newAirplane.copy(maxRangeInKm = -1)
    repo
      .createAirplane(newAirplaneWithNegativeMaxRange)
      .unsafeRunSync()
      .left
      .value shouldBe EntryCheckFailed
  }

  "Updating an airplane" should "work and return the updated airplane ID" in {
    val original = Airplane.fromCreate(originalExpectedAirplanes.length + 1, newAirplane)
    val updated = original.copy(capacity = original.capacity + 100)
    val returned = repo.updateAirplane(updated).unsafeRunSync().value.value
    returned shouldBe updated.id
  }

  it should "also allow changing the name field" in {
    val original = Airplane.fromCreate(originalExpectedAirplanes.length + 1, newAirplane)
    val updated = original.copy(name = s"${original.name}_updated")
    val returned = repo.updateAirplane(updated).unsafeRunSync().value.value
    returned shouldBe updated.id
  }

  it should "throw an error if we update a non-existing airplane" in {
    val updated = Airplane.fromCreate(idNotPresent, newAirplane)
    repo.updateAirplane(updated).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val updated = Airplane.fromCreate(originalExpectedAirplanes.length + 1, newAirplane)
    val updatedWithNonExistingManufacturerId = updated.copy(manufacturerId = idNotPresent)
    repo
      .updateAirplane(updatedWithNonExistingManufacturerId)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  "Patching an airplane" should "work and return the patched airplane" in {
    val original = Airplane.fromCreate(originalExpectedAirplanes.length + 1, newAirplane)
    val patch = AirplanePatch(name = Some(s"${original.name}_patched"), None, None, None)
    val patched = Airplane.fromPatch(original.id, patch, original)
    val returned = repo.partiallyUpdateAirplane(original.id, patch).unsafeRunSync().value.value
    returned shouldBe patched
  }

  it should "throw an error if we patch a non-existing airplane" in {
    val patch = AirplanePatch(name = Some("Something"), None, None, None)
    repo
      .partiallyUpdateAirplane(idNotPresent, patch)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the manufacturer does not exist" in {
    val original = Airplane.fromCreate(originalExpectedAirplanes.length + 1, newAirplane)
    val patch = AirplanePatch(None, manufacturerId = Some(idNotPresent), None, None)
    repo
      .partiallyUpdateAirplane(original.id, patch)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  "Removing an airplane" should "work correctly" in {
    val id = originalExpectedAirplanes.length + 1
    repo.removeAirplane(id).unsafeRunSync().value.value shouldBe ()
    repo.doesAirplaneExist(id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing airplane" in {
    repo.removeAirplane(idNotPresent).unsafeRunSync().left.value shouldBe EntryNotFound(
      idNotPresent
    )
  }

}
