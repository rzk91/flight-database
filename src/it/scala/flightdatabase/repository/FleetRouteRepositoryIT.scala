package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.fleet_route.FleetRoute
import flightdatabase.testutils.RepositoryCheck

final class FleetRouteRepositoryIT extends RepositoryCheck {

  "Selecting all fleet routes" should "return the correct detailed list" in {
    val fleetRoutes = {
      for {
        repo           <- FleetRouteRepository.make[IO]
        allFleetRoutes <- repo.getFleetRoutes
      } yield allFleetRoutes
    }.unsafeRunSync().value.value

    fleetRoutes should not be empty
    fleetRoutes should contain only (
      FleetRoute(1, 1, "LH754", 1, 2),
      FleetRoute(2, 1, "LH755", 2, 1),
      FleetRoute(3, 5, "EK565", 2, 3),
      FleetRoute(4, 5, "EK566", 3, 2),
      FleetRoute(5, 4, "EK47", 3, 1),
      FleetRoute(6, 4, "EK46", 1, 3)
    )
  }
}
