package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.country.Country

trait CountryFixtures {

  val countries: Nel[Country] = Nel.of(
    Country(1, "India", "IN", "IND", 91, Some(".in"), 7, Some(1), Some(3), 1, "Indian"),
    Country(2, "Germany", "DE", "DEU", 49, Some(".de"), 2, None, None, 2, "German"),
    Country(3, "Sweden", "SE", "SWE", 46, Some(".se"), 4, None, None, 3, "Swede"),
    Country(
      4,
      "United Arab Emirates",
      "AE",
      "ARE",
      971,
      Some(".ae"),
      5,
      Some(1),
      None,
      4,
      "Emirati"
    ),
    Country(5, "Netherlands", "NL", "NLD", 31, Some(".nl"), 6, None, None, 2, "Dutch"),
    Country(
      6,
      "United States of America",
      "US",
      "USA",
      1,
      Some(".us"),
      1,
      None,
      None,
      5,
      "US citizen"
    )
  )
}

object country extends CountryFixtures
