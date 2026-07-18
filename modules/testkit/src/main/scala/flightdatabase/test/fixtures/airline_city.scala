package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airline_city.AirlineCity

trait AirlineCityFixtures {

  val airlineCities: Nel[AirlineCity] = Nel.of(
    AirlineCity(1, 1, 2),
    AirlineCity(2, 2, 4)
  )
}

object airline_city extends AirlineCityFixtures
