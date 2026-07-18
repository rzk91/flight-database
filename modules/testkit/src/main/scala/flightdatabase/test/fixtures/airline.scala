package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airline.Airline

trait AirlineFixtures {

  val airlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )
}

object airline extends AirlineFixtures
