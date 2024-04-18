package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airplane.Airplane
import flightdatabase.testutils.RepositoryCheck

final class AirplaneRepositoryIT extends RepositoryCheck {

  lazy val repo: AirplaneRepository[IO] = AirplaneRepository.make[IO].unsafeRunSync()

  val expectedAirplanes: List[Airplane] = List(
    Airplane(1, "A380", 1, 853, 14800),
    Airplane(2, "747-8", 2, 410, 14310),
    Airplane(3, "A320neo", 1, 194, 6300),
    Airplane(4, "787-8", 2, 248, 13530)
  )
  val idNotPresent: Long = 10
  val idDefinitelyNotPresent: Long = 1039495454540034858L

  "Checking if an airplane exists" should "return a valid result" in {
    def airplaneExists(id: Long): Boolean =
      repo.doesAirplaneExist(id).unsafeRunSync()
    airplaneExists(1) shouldBe true
    airplaneExists(idNotPresent) shouldBe false
    airplaneExists(idDefinitelyNotPresent) shouldBe false
  }

  "Selecting all airplanes" should "return the correct detailed list" in {
    val airplanes = repo.getAirplanes.unsafeRunSync().value.value

    airplanes should not be empty
    airplanes should contain only (expectedAirplanes: _*)
  }

  it should "return only names if so required" in {
    val airplanesOnlyNames = repo.getAirplanesOnlyNames.unsafeRunSync().value.value
    airplanesOnlyNames should not be empty
    airplanesOnlyNames should contain only (expectedAirplanes.map(_.name): _*)
  }

  "Selecting an airplane by id" should "return the correct entry" in {
    def airplaneById(id: Long): ApiResult[Airplane] = repo.getAirplane(id).unsafeRunSync()

    expectedAirplanes.foreach { airplane =>
      airplaneById(airplane.id).value.value shouldBe airplane
    }
    airplaneById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent.toString)
    airplaneById(idDefinitelyNotPresent).left.value shouldBe EntryNotFound(
      idDefinitelyNotPresent.toString
    )
  }

}
