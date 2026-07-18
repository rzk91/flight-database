package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airline_route.AirlineRoute

trait AirlineRouteFixtures {
  val airlineRoutes: Nel[AirlineRoute] = Nel.of(
    AirlineRoute(1, 1, "LH754", 1, 2),
    AirlineRoute(2, 1, "LH755", 2, 1),
    AirlineRoute(3, 5, "EK565", 2, 3),
    AirlineRoute(4, 5, "EK566", 3, 2),
    AirlineRoute(5, 4, "EK47", 3, 1),
    AirlineRoute(6, 4, "EK46", 1, 3)
  )
}

object airlineRoute extends AirlineRouteFixtures
