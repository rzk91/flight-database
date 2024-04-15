package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.airplane.Airplane
import flightdatabase.testutils.RepositoryCheck

final class AirplaneRepositoryIT extends RepositoryCheck {

  "Selecting all airplanes" should "return the correct detailed list" in {
    val airplanes = {
      for {
        repo         <- AirplaneRepository.make[IO]
        allAirplanes <- repo.getAirplanes
      } yield allAirplanes
    }.unsafeRunSync().value.value

    airplanes should not be empty
    airplanes should contain only (
      Airplane(1, "A380", 1, 853, 14800),
      Airplane(2, "747-8", 2, 410, 14310),
      Airplane(3, "A320neo", 1, 194, 6300),
      Airplane(4, "787-8", 2, 248, 13530)
    )
  }

}
