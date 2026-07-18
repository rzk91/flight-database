package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.manufacturer.Manufacturer

trait ManufacturerFixtures {
  val manufacturers: Nel[Manufacturer] = Nel.of(
    Manufacturer(1, "Airbus", 5),
    Manufacturer(2, "Boeing", 6)
  )
}

object manufacturer extends ManufacturerFixtures
