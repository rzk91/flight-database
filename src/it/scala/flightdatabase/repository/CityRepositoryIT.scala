package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.city.City
import flightdatabase.testutils.RepositoryCheck

class CityRepositoryIT extends RepositoryCheck {

  "Selecting all cities" should "return the correct detailed list" in {
    val cities = {
      for {
        repo      <- CityRepository.make[IO]
        allCities <- repo.getCities
      } yield allCities
    }.unsafeRunSync().value.value

    cities should not be empty
    cities should contain only (
      City(
        1,
        "Bangalore",
        1,
        capital = false,
        13193000,
        BigDecimal("12.978889"),
        BigDecimal("77.591667"),
        "Asia/Kolkata"
      ),
      City(
        2,
        "Frankfurt am Main",
        2,
        capital = false,
        791000,
        BigDecimal("50.110556"),
        BigDecimal("8.682222"),
        "Europe/Berlin"
      ),
      City(
        3,
        "Berlin",
        2,
        capital = true,
        3571000,
        BigDecimal("52.52"),
        BigDecimal("13.405"),
        "Europe/Berlin"
      ),
      City(
        4,
        "Dubai",
        4,
        capital = false,
        3490000,
        BigDecimal("23.5"),
        BigDecimal("54.5"),
        "Asia/Dubai"
      ),
      City(
        5,
        "Leiden",
        5,
        capital = false,
        125100,
        BigDecimal("52.16"),
        BigDecimal("4.49"),
        "Europe/Amsterdam"
      ),
      City(
        6,
        "Chicago",
        6,
        capital = false,
        8901000,
        BigDecimal("41.85003"),
        BigDecimal("-87.65005"),
        "America/Chicago"
      )
    )
  }
}
