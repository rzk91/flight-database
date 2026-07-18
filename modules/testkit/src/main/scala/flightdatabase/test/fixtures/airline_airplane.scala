package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airline_airplane.AirlineAirplane

trait AirlineAirplaneFixtures {

  val airlineAirplanes: Nel[AirlineAirplane] = Nel.of(
    AirlineAirplane(1, 1, 2),
    AirlineAirplane(2, 1, 1),
    AirlineAirplane(3, 1, 3),
    AirlineAirplane(4, 2, 1),
    AirlineAirplane(5, 2, 3)
  )
}

object airline_airplane extends AirlineAirplaneFixtures
