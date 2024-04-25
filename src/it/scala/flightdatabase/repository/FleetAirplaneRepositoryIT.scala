package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.domain.fleet_airplane.FleetAirplaneCreate
import flightdatabase.domain.fleet_airplane.FleetAirplanePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits.enrichIOOperation
import org.scalatest.Inspectors.forAll

final class FleetAirplaneRepositoryIT extends RepositoryCheck {

  lazy val repo: FleetAirplaneRepository[IO] = FleetAirplaneRepository.make[IO].unsafeRunSync()

  val originalFleetAirplanes: List[FleetAirplane] = List(
    FleetAirplane(1, 1, 2),
    FleetAirplane(2, 1, 1),
    FleetAirplane(3, 1, 3),
    FleetAirplane(4, 2, 1),
    FleetAirplane(5, 2, 3)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not Present"
  val veryLongIdNotPresent: Long = 1000000000000000000L

  val fleetIdMap: Map[Long, (String, String)] = Map(
    1L -> ("Lufthansa", "LH"),
    2L -> ("Emirates", "EK")
  )

  val airplaneIdMap: Map[Long, String] = Map(1L -> "A380", 2L -> "747-8", 3L -> "A320neo")

  val newFleetAirplane: FleetAirplaneCreate = FleetAirplaneCreate(2, 2) // EK -> 747-8
  val updatedAirplane: Long = 4                                         // EK -> 787-8
  val patchedAirplane: Long = 2                                         // Back to EK -> 747-8

  "Checking if a fleet-airplane exists" should "return a valid result" in {
    def fleetAirplaneExists(id: Long): Boolean = repo.doesFleetAirplaneExist(id).unsafeRunSync()
    forAll(originalFleetAirplanes.map(_.id))(id => fleetAirplaneExists(id) shouldBe true)
    fleetAirplaneExists(idNotPresent) shouldBe false
    fleetAirplaneExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all fleet airplanes" should "return the correct detailed list" in {
    val fleetAirplanes = repo.getFleetAirplanes.value

    fleetAirplanes should not be empty
    fleetAirplanes should contain only (originalFleetAirplanes: _*)
  }

  "Selecting a fleet airplane by id" should "return the correct detailed fleet airplane" in {
    forAll(originalFleetAirplanes)(fleetAirplane =>
      repo.getFleetAirplane(fleetAirplane.id).value shouldBe fleetAirplane
    )
    repo.getFleetAirplane(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getFleetAirplane(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a fleet airplane by fleet and airplane IDs" should "return the correct detailed fleet airplane" in {
    def f(fleetId: Long, airplaneId: Long): IO[ApiResult[FleetAirplane]] =
      repo.getFleetAirplane(fleetId, airplaneId)

    forAll(originalFleetAirplanes)(fleetAirplane =>
      f(fleetAirplane.fleetId, fleetAirplane.airplaneId).value shouldBe fleetAirplane
    )

    f(idNotPresent, 1).error shouldBe EntryListEmpty
    f(1, veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting fleet airplanes by other fields" should "return the corresponding entries" in {
    def fleetAirplaneByFleetId(id: Long): IO[ApiResult[List[FleetAirplane]]] =
      repo.getFleetAirplanes("fleet_id", id)

    def fleetAirplaneByAirplaneId(id: Long): IO[ApiResult[List[FleetAirplane]]] =
      repo.getFleetAirplanes("airplane_id", id)

    val distinctFleetIds = originalFleetAirplanes.map(_.fleetId).distinct
    val distinctAirplaneIds = originalFleetAirplanes.map(_.airplaneId).distinct

    forAll(distinctFleetIds) { fleetId =>
      fleetAirplaneByFleetId(fleetId).value should contain only (
        originalFleetAirplanes.filter(_.fleetId == fleetId): _*
      )
    }

    forAll(distinctAirplaneIds) { airplaneId =>
      fleetAirplaneByAirplaneId(airplaneId).value should contain only (
        originalFleetAirplanes.filter(_.airplaneId == airplaneId): _*
      )
    }

    fleetAirplaneByFleetId(idNotPresent).error shouldBe EntryListEmpty
    fleetAirplaneByFleetId(veryLongIdNotPresent).error shouldBe EntryListEmpty
    fleetAirplaneByAirplaneId(idNotPresent).error shouldBe EntryListEmpty
    fleetAirplaneByAirplaneId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting fleet airplanes by external fields" should "return the corresponding entries" in {
    def fleetAirplanesByFleetName(name: String): IO[ApiResult[List[FleetAirplane]]] =
      repo.getFleetAirplanesByFleetName(name)

    def fleetAirplanesByAirplaneName(name: String): IO[ApiResult[List[FleetAirplane]]] =
      repo.getFleetAirplanesByAirplaneName(name)

    def fleetAirplanesByFleetIso(iso: String): IO[ApiResult[List[FleetAirplane]]] =
      repo.getFleetAirplanesByExternal[Fleet, String]("iso2", iso)

    forAll(fleetIdMap) {
      case (id, (name, iso)) =>
        fleetAirplanesByFleetName(name).value should contain only (
          originalFleetAirplanes.filter(_.fleetId == id): _*
        )
        fleetAirplanesByFleetIso(iso).value should contain only (
          originalFleetAirplanes.filter(_.fleetId == id): _*
        )
    }

    forAll(airplaneIdMap) {
      case (id, name) =>
        fleetAirplanesByAirplaneName(name).value should contain only (
          originalFleetAirplanes.filter(_.airplaneId == id): _*
        )
    }

    fleetAirplanesByFleetName(valueNotPresent).error shouldBe EntryListEmpty
    fleetAirplanesByFleetIso(valueNotPresent).error shouldBe EntryListEmpty
    fleetAirplanesByAirplaneName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Creating a new fleet-airplane" should "not work if the fleet or airplane does not exist" in {
    val invalidFleetAirplanes = List(
      newFleetAirplane.copy(fleetId = idNotPresent),
      newFleetAirplane.copy(airplaneId = idNotPresent)
    )

    forAll(invalidFleetAirplanes) { fleetAirplane =>
      repo
        .createFleetAirplane(fleetAirplane)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the combination of fleet and airplane already exists" in {
    val existingFleetAirplane = originalFleetAirplanes.head
    repo
      .createFleetAirplane(
        FleetAirplaneCreate(existingFleetAirplane.fleetId, existingFleetAirplane.airplaneId)
      )
      .error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val newFleetAirplaneId = repo.createFleetAirplane(newFleetAirplane).value
    val newFleetAirplaneFromDb = repo.getFleetAirplane(newFleetAirplaneId).value

    newFleetAirplaneFromDb shouldBe FleetAirplane.fromCreate(newFleetAirplaneId, newFleetAirplane)
  }

  "Updating a fleet-airplane" should "not work if the fleet or airplane does not exist" in {
    val existingFleetAirplane = originalFleetAirplanes.head
    val invalidFleetAirplanes = List(
      existingFleetAirplane.copy(fleetId = idNotPresent),
      existingFleetAirplane.copy(airplaneId = idNotPresent)
    )

    forAll(invalidFleetAirplanes) { fleetAirplane =>
      repo
        .updateFleetAirplane(fleetAirplane)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the fleet-airplane combination already exists" in {
    val existingFleetAirplaneId = originalFleetAirplanes.head.id
    repo
      .updateFleetAirplane(FleetAirplane.fromCreate(existingFleetAirplaneId, newFleetAirplane))
      .error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val existingFleetAirplane = repo
      .getFleetAirplane(newFleetAirplane.fleetId, newFleetAirplane.airplaneId)
      .value

    val updated = existingFleetAirplane.copy(airplaneId = updatedAirplane)
    repo.updateFleetAirplane(updated).value shouldBe updated.id

    val updatedFleetAirplane = repo.getFleetAirplane(updated.id).value
    updatedFleetAirplane shouldBe updated
  }

  "Patching a fleet airplane" should "not work if the fleet or airplane does not exist" in {
    val existingFleetAirplaneId = originalFleetAirplanes.head.id

    val invalidFleetAirplanes = List(
      FleetAirplanePatch(fleetId = Some(veryLongIdNotPresent)),
      FleetAirplanePatch(airplaneId = Some(idNotPresent))
    )

    forAll(invalidFleetAirplanes) { patch =>
      repo
        .partiallyUpdateFleetAirplane(existingFleetAirplaneId, patch)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if we patch a non-existing fleet-airplane entry" in {
    val patched = FleetAirplanePatch(fleetId = Some(1))
    repo
      .partiallyUpdateFleetAirplane(idNotPresent, patched)
      .error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not work if the fleet-airplane combination already exists" in {
    val existingFleetAirplane = originalFleetAirplanes.find(_.fleetId == newFleetAirplane.fleetId)

    existingFleetAirplane.foreach { fa =>
      val patched = FleetAirplanePatch(airplaneId = Some(updatedAirplane))
      repo
        .partiallyUpdateFleetAirplane(fa.id, patched)
        .error shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val existingFleetAirplane = repo
      .getFleetAirplane(newFleetAirplane.fleetId, updatedAirplane)
      .value

    val patched = FleetAirplanePatch(airplaneId = Some(patchedAirplane))
    val patchedFleetAirplane = repo
      .partiallyUpdateFleetAirplane(existingFleetAirplane.id, patched)
      .value

    patchedFleetAirplane shouldBe existingFleetAirplane.copy(airplaneId = patchedAirplane)
  }

  "Removing a fleet-airplane" should "work correctly if it exists" in {
    val existingFleetAirplane = repo
      .getFleetAirplane(newFleetAirplane.fleetId, patchedAirplane)
      .value

    repo
      .removeFleetAirplane(existingFleetAirplane.id)
      .value shouldBe ()

    repo
      .getFleetAirplane(existingFleetAirplane.id)
      .error shouldBe EntryNotFound(existingFleetAirplane.id)
  }

  "Removing a fleet-airplane" should "not work if the fleet-airplane does not exist" in {
    repo
      .removeFleetAirplane(idNotPresent)
      .error shouldBe EntryNotFound(idNotPresent)
  }
}
