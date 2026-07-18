package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airplane.Airplane

trait AirplaneFixtures {

  val airplanes: Nel[Airplane] = Nel.of(
    Airplane(1, "A380", 1, 853, 14800, 903),
    Airplane(2, "747-8", 2, 410, 14310, 907),
    Airplane(3, "A320neo", 1, 194, 6300, 828),
    Airplane(4, "787-8", 2, 248, 13530, 903)
  )
}

object airplane extends AirplaneFixtures
