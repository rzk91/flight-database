package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.fleet.Fleet
import flightdatabase.testutils.RepositoryCheck

final class FleetRepositoryIT extends RepositoryCheck {

  "Selecting all fleets" should "return the correct detailed list" in {
    val fleets = {
      for {
        repo      <- FleetRepository.make[IO]
        allFleets <- repo.getFleets
      } yield allFleets
    }.unsafeRunSync().value.value

    fleets should not be empty
    fleets should contain only (
      Fleet(1, "Lufthansa", "LH", "DLH", "Lufthansa", 1),
      Fleet(2, "Emirates", "EK", "UAE", "Emirates", 3)
    )
  }
}
