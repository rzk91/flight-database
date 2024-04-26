package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.testutils.RepositoryCheck

final class AirlineRouteRepositoryIT extends RepositoryCheck {

  "Selecting all airline routes" should "return the correct detailed list" in {
    val airlineRoutes = {
      for {
        repo             <- AirlineRouteRepository.make[IO]
        allAirlineRoutes <- repo.getAirlineRoutes
      } yield allAirlineRoutes
    }.unsafeRunSync().value.value

    airlineRoutes should not be empty
    airlineRoutes should contain only (
      AirlineRoute(1, 1, "LH754", 1, 2),
      AirlineRoute(2, 1, "LH755", 2, 1),
      AirlineRoute(3, 5, "EK565", 2, 3),
      AirlineRoute(4, 5, "EK566", 3, 2),
      AirlineRoute(5, 4, "EK47", 3, 1),
      AirlineRoute(6, 4, "EK46", 1, 3)
    )
  }
}
