package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.airport.Airport
import flightdatabase.airport.TaxiDuration

trait AirportFixtures {

  val airports: Nel[Airport] = Nel.of(
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
      junction = true,
      latitude = BigDecimal("50.0333"),
      longitude = BigDecimal("8.5706"),
      taxiOutDuration = TaxiDuration(18),
      taxiInDuration = TaxiDuration(8)
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
      junction = false,
      latitude = BigDecimal("13.1986"),
      longitude = BigDecimal("77.7066"),
      taxiOutDuration = TaxiDuration(12),
      taxiInDuration = TaxiDuration(6)
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
      junction = true,
      latitude = BigDecimal("25.2532"),
      longitude = BigDecimal("55.3657"),
      taxiOutDuration = TaxiDuration(15),
      taxiInDuration = TaxiDuration(7)
    )
  )
}

object airport extends AirportFixtures
