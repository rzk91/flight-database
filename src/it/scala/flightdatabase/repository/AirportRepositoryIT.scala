package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.airport.Airport
import flightdatabase.testutils.RepositoryCheck

class AirportRepositoryIT extends RepositoryCheck {

  "Selecting all airports" should "return the correct detailed list" in {
    val airports = {
      for {
        repo        <- AirportRepository.make[IO]
        allAirports <- repo.getAirports
      } yield allAirports
    }.unsafeRunSync().value.value

    airports should not be empty
    airports should contain only (
      Airport(
        1,
        "Frankfurt am Main Airport",
        "EDDF",
        "FRA",
        2,
        4,
        3,
        65000000,
        international = true,
        junction = true
      ),
      Airport(
        2,
        "Kempegowda International Airport",
        "VOBL",
        "BLR",
        1,
        2,
        2,
        16800000,
        international = true,
        junction = false
      ),
      Airport(
        3,
        "Dubai International Airport",
        "OMDB",
        "DXB",
        4,
        2,
        3,
        92500000,
        international = true,
        junction = false
      )
    )
  }

}
