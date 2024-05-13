package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate
import flightdatabase.domain.airline_airplane.AirlineAirplanePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits._
import flightdatabase.utils.implicits.iterableToRichIterable
import org.scalatest.Inspectors.forAll

final class AirlineAirplaneRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineAirplaneRepository[IO] = AirlineAirplaneRepository.make[IO].unsafeRunSync()

  val originalAirlineAirplanes: Nel[AirlineAirplane] = Nel.of(
    AirlineAirplane(1, 1, 2),
    AirlineAirplane(2, 1, 1),
    AirlineAirplane(3, 1, 3),
    AirlineAirplane(4, 2, 1),
    AirlineAirplane(5, 2, 3)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not Present"
  val veryLongIdNotPresent: Long = 1000000000000000000L

  // airlineId -> (name, iata)
  val airlineIdMap: Map[Long, (String, String)] = Map(
    1L -> ("Lufthansa", "LH"),
    2L -> ("Emirates", "EK")
  )

  // airplaneId -> (name, capacity)
  val airplaneIdMap: Map[Long, (String, Int)] =
    Map(1L -> ("A380", 853), 2L -> ("747-8", 410), 3L -> ("A320neo", 194))

  val newAirlineAirplane: AirlineAirplaneCreate = AirlineAirplaneCreate(2, 2) // EK -> 747-8
  val updatedAirplane: Long = 4                                               // EK -> 787-8
  val patchedAirplane: Long = 2                                               // Back to EK -> 747-8

  "Checking if a airline-airplane exists" should "return a valid result" in {
    def airlineAirplaneExists(id: Long): Boolean = repo.doesAirlineAirplaneExist(id).unsafeRunSync()
    forAll(originalAirlineAirplanes.map(_.id))(id => airlineAirplaneExists(id) shouldBe true)
    airlineAirplaneExists(idNotPresent) shouldBe false
    airlineAirplaneExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airline airplanes" should "return the correct detailed list" in {
    repo.getAirlineAirplanes.value should contain only (originalAirlineAirplanes.toList: _*)
  }

  "Selecting a airline airplane by id" should "return the correct detailed airline airplane" in {
    forAll(originalAirlineAirplanes)(airlineAirplane =>
      repo.getAirlineAirplane(airlineAirplane.id).value shouldBe airlineAirplane
    )
    repo.getAirlineAirplane(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirlineAirplane(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a airline airplane by airline and airplane IDs" should "return the correct detailed airline airplane" in {
    def f(airlineId: Long, airplaneId: Long): IO[ApiResult[AirlineAirplane]] =
      repo.getAirlineAirplane(airlineId, airplaneId)

    forAll(originalAirlineAirplanes)(airlineAirplane =>
      f(airlineAirplane.airlineId, airlineAirplane.airplaneId).value shouldBe airlineAirplane
    )

    f(idNotPresent, 1).error shouldBe EntryListEmpty
    f(1, veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline airplanes by other fields" should "return the corresponding entries" in {
    def airlineAirplaneByAirlineId(id: Long): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesBy("airline_id", Nel.one(id), Operator.Equals)

    def airlineAirplaneByAirplaneId(id: Long): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesBy("airplane_id", Nel.one(id), Operator.Equals)

    val distinctAirlineIds = originalAirlineAirplanes.map(_.airlineId).distinct
    val distinctAirplaneIds = originalAirlineAirplanes.map(_.airplaneId).distinct

    forAll(distinctAirlineIds) { airlineId =>
      airlineAirplaneByAirlineId(airlineId).value should contain only (
        originalAirlineAirplanes.filter(_.airlineId == airlineId): _*
      )
    }

    forAll(distinctAirplaneIds) { airplaneId =>
      airlineAirplaneByAirplaneId(airplaneId).value should contain only (
        originalAirlineAirplanes.filter(_.airplaneId == airplaneId): _*
      )
    }

    airlineAirplaneByAirlineId(idNotPresent).error shouldBe EntryListEmpty
    airlineAirplaneByAirlineId(veryLongIdNotPresent).error shouldBe EntryListEmpty
    airlineAirplaneByAirplaneId(idNotPresent).error shouldBe EntryListEmpty
    airlineAirplaneByAirplaneId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline airplanes by external fields" should "return the corresponding entries" in {
    def airlineAirplanesByAirlineName(name: String): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesByAirline("name", Nel.one(name), Operator.Equals)

    def airlineAirplanesByAirplaneName(name: String): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesByAirplane("name", Nel.one(name), Operator.Equals)

    def airlineAirplanesByAirlineIata(iata: String): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesByAirline("iata", Nel.one(iata), Operator.Equals)

    def airlineAirplanesByAirplaneCapacity(capacity: Int): IO[ApiResult[Nel[AirlineAirplane]]] =
      repo.getAirlineAirplanesByAirplane(
        "capacity",
        Nel.one(capacity),
        Operator.GreaterThanOrEqualTo
      )

    forAll(airlineIdMap) {
      case (id, (name, iso)) =>
        airlineAirplanesByAirlineName(name).value should contain only (
          originalAirlineAirplanes.filter(_.airlineId == id): _*
        )
        airlineAirplanesByAirlineIata(iso).value should contain only (
          originalAirlineAirplanes.filter(_.airlineId == id): _*
        )
    }

    forAll(airplaneIdMap) {
      case (id, (name, _)) =>
        airlineAirplanesByAirplaneName(name).value should contain only (
          originalAirlineAirplanes.filter(_.airplaneId == id): _*
        )
    }

    val leastCapacity = 200

    airlineAirplanesByAirplaneCapacity(leastCapacity).value should contain only (
      originalAirlineAirplanes.filter(aa =>
        airplaneIdMap
          .withFilter { case (_, (_, capacity)) => capacity >= leastCapacity }
          .map { case (id, _) => id }
          .containsElement(aa.airplaneId)
      ): _*
    )

    airlineAirplanesByAirlineName(valueNotPresent).error shouldBe EntryListEmpty
    airlineAirplanesByAirlineIata(valueNotPresent).error shouldBe EntryListEmpty
    airlineAirplanesByAirplaneName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Creating a new airline-airplane" should "not work if the airline or airplane does not exist" in {
    val invalidAirlineAirplanes = List(
      newAirlineAirplane.copy(airlineId = idNotPresent),
      newAirlineAirplane.copy(airplaneId = idNotPresent)
    )

    forAll(invalidAirlineAirplanes) { airlineAirplane =>
      repo
        .createAirlineAirplane(airlineAirplane)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the combination of airline and airplane already exists" in {
    val existingAirlineAirplane = originalAirlineAirplanes.head
    repo
      .createAirlineAirplane(
        AirlineAirplaneCreate(existingAirlineAirplane.airlineId, existingAirlineAirplane.airplaneId)
      )
      .error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val newAirlineAirplaneId = repo.createAirlineAirplane(newAirlineAirplane).value
    val newAirlineAirplaneFromDb = repo.getAirlineAirplane(newAirlineAirplaneId).value

    newAirlineAirplaneFromDb shouldBe AirlineAirplane.fromCreate(
      newAirlineAirplaneId,
      newAirlineAirplane
    )
  }

  "Updating a airline-airplane" should "not work if the airline or airplane does not exist" in {
    val existingAirlineAirplane = originalAirlineAirplanes.head
    val invalidAirlineAirplanes = List(
      existingAirlineAirplane.copy(airlineId = idNotPresent),
      existingAirlineAirplane.copy(airplaneId = idNotPresent)
    )

    forAll(invalidAirlineAirplanes) { airlineAirplane =>
      repo
        .updateAirlineAirplane(airlineAirplane)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the airline-airplane combination already exists" in {
    val existingAirlineAirplaneId = originalAirlineAirplanes.head.id
    repo
      .updateAirlineAirplane(
        AirlineAirplane.fromCreate(existingAirlineAirplaneId, newAirlineAirplane)
      )
      .error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val existingAirlineAirplane = repo
      .getAirlineAirplane(newAirlineAirplane.airlineId, newAirlineAirplane.airplaneId)
      .value

    val updated = existingAirlineAirplane.copy(airplaneId = updatedAirplane)
    repo.updateAirlineAirplane(updated).value shouldBe updated.id

    val updatedAirlineAirplane = repo.getAirlineAirplane(updated.id).value
    updatedAirlineAirplane shouldBe updated
  }

  "Patching a airline airplane" should "not work if the airline or airplane does not exist" in {
    val existingAirlineAirplaneId = originalAirlineAirplanes.head.id

    val invalidAirlineAirplanes = List(
      AirlineAirplanePatch(airlineId = Some(veryLongIdNotPresent)),
      AirlineAirplanePatch(airplaneId = Some(idNotPresent))
    )

    forAll(invalidAirlineAirplanes) { patch =>
      repo
        .partiallyUpdateAirlineAirplane(existingAirlineAirplaneId, patch)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if we patch a non-existing airline-airplane entry" in {
    val patched = AirlineAirplanePatch(airlineId = Some(1))
    repo
      .partiallyUpdateAirlineAirplane(idNotPresent, patched)
      .error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not work if the airline-airplane combination already exists" in {
    val existingAirlineAirplane =
      originalAirlineAirplanes.find(_.airlineId == newAirlineAirplane.airlineId)

    existingAirlineAirplane.foreach { fa =>
      val patched = AirlineAirplanePatch(airplaneId = Some(updatedAirplane))
      repo
        .partiallyUpdateAirlineAirplane(fa.id, patched)
        .error shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val existingAirlineAirplane = repo
      .getAirlineAirplane(newAirlineAirplane.airlineId, updatedAirplane)
      .value

    val patched = AirlineAirplanePatch(airplaneId = Some(patchedAirplane))
    val patchedAirlineAirplane = repo
      .partiallyUpdateAirlineAirplane(existingAirlineAirplane.id, patched)
      .value

    patchedAirlineAirplane shouldBe existingAirlineAirplane.copy(airplaneId = patchedAirplane)
  }

  "Removing an airline-airplane" should "work correctly if it exists" in {
    val existingAirlineAirplane = repo
      .getAirlineAirplane(newAirlineAirplane.airlineId, patchedAirplane)
      .value

    repo
      .removeAirlineAirplane(existingAirlineAirplane.id)
      .value shouldBe ()

    repo
      .getAirlineAirplane(existingAirlineAirplane.id)
      .error shouldBe EntryNotFound(existingAirlineAirplane.id)
  }

  "Removing a airline-airplane" should "not work if the airline-airplane does not exist" in {
    repo
      .removeAirlineAirplane(idNotPresent)
      .error shouldBe EntryNotFound(idNotPresent)
  }
}
