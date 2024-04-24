package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.testutils.RepositoryCheck

final class FleetAirplaneRepositoryIT extends RepositoryCheck {

  "Selecting all fleet airplanes" should "return the correct detailed list" in {
    val fleetAirplanes = {
      for {
        repo <- FleetAirplaneRepository.make[IO]
        all  <- repo.getFleetAirplanes
      } yield all
    }.unsafeRunSync().value.value

    fleetAirplanes should not be empty
    fleetAirplanes should contain only (
      FleetAirplane(1, 1, 2),
      FleetAirplane(2, 1, 1),
      FleetAirplane(3, 1, 3),
      FleetAirplane(4, 2, 1),
      FleetAirplane(5, 2, 3)
    )
  }
}
