package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.country.Country
import flightdatabase.testutils.RepositoryCheck

class CountryRepositoryIT extends RepositoryCheck {

  "Selecting all countries" should "return the correct detailed list" in {
    val countries = {
      for {
        repo         <- CountryRepository.make[IO]
        allCountries <- repo.getCountries
      } yield allCountries
    }.unsafeRunSync().value.value

    countries should not be empty
    countries should contain only (
      Country(1, "India", "IN", "IND", 91, Some(".co.in"), 7, Some(1), Some(3), 1, "Indian"),
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

}
