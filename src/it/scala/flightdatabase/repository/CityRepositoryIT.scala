package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityCreate
import flightdatabase.testutils.RepositoryCheck

class CityRepositoryIT extends RepositoryCheck {

  lazy val repo: CityRepository[IO] = CityRepository.make[IO].unsafeRunSync()

  val originalCities: List[City] = List(
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

  val countryToIdMap: Map[String, Long] = Map(
    "India"                    -> 1,
    "Germany"                  -> 2,
    "United Arab Emirates"     -> 4,
    "Netherlands"              -> 5,
    "United States of America" -> 6
  )

  val idNotPresent: Long = 10
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  val newCity: CityCreate = CityCreate(
    "Munich",
    2,
    capital = false,
    1488000,
    BigDecimal("48.137222"),
    BigDecimal("11.575556"),
    "Europe/Berlin"
  )

  val newBerlinInUSA: CityCreate = CityCreate(
    "Berlin",
    6,
    capital = false,
    8901000,
    BigDecimal("41.85003"),
    BigDecimal("-87.65005"),
    "America/Chicago"
  )

  "Checking if a city exists" should "return a valid result" in {
    def cityExists(id: Long): Boolean = repo.doesCityExist(id).unsafeRunSync()
    cityExists(1) shouldBe true
    cityExists(idNotPresent) shouldBe false
    cityExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all cities" should "return the correct detailed list" in {
    val cities = repo.getCities.unsafeRunSync().value.value

    cities should not be empty
    cities should contain only (originalCities: _*)
  }

  it should "return only names if so required" in {
    val citiesOnlyNames = repo.getCitiesOnlyNames.unsafeRunSync().value.value
    citiesOnlyNames should not be empty
    citiesOnlyNames should contain only (originalCities.map(_.name): _*)
  }

  "Selecting a city by id" should "return the correct entry" in {
    def cityById(id: Long): ApiResult[City] = repo.getCity(id).unsafeRunSync()

    originalCities.foreach(city => cityById(city.id).value.value shouldBe city)
    cityById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent)
    cityById(veryLongIdNotPresent).left.value shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a city by other fields" should "return the corresponding entries" in {
    def cityByName(name: String): ApiResult[List[City]] =
      repo.getCities("name", name).unsafeRunSync()

    def cityByCountryId(id: Long): ApiResult[List[City]] =
      repo.getCities("country_id", id).unsafeRunSync()

    val distinctCountryIds = originalCities.map(_.countryId).distinct

    originalCities.foreach(city => cityByName(city.name).value.value should contain only city)

    distinctCountryIds.foreach { id =>
      val expectedCities = originalCities.filter(_.countryId == id)
      cityByCountryId(id).value.value should contain only (expectedCities: _*)
    }

    cityByName(valueNotPresent).left.value shouldBe EntryListEmpty
    cityByCountryId(idNotPresent).left.value shouldBe EntryListEmpty
    cityByCountryId(veryLongIdNotPresent).left.value shouldBe EntryListEmpty
  }

  "Selecting a city by country name" should "return the corresponding entries" in {
    def cityByCountry(name: String): ApiResult[List[City]] =
      repo.getCitiesByCountry(name).unsafeRunSync()

    countryToIdMap.foreach {
      case (country, id) =>
        cityByCountry(country).value.value should contain only (
          originalCities.filter(_.countryId == id): _*
        )
    }

    cityByCountry(valueNotPresent).left.value shouldBe EntryListEmpty
  }

  "Creating a new city" should "not take place if fields don't satisfy their criteria" in {
    val invalidCity = newCity.copy(name = "")
    repo.createCity(invalidCity).unsafeRunSync().left.value shouldBe EntryCheckFailed

    val invalidTimezone = newCity.copy(timezone = "")
    repo.createCity(invalidTimezone).unsafeRunSync().left.value shouldBe EntryCheckFailed

    val invalidPopulation = newCity.copy(population = -1)
    repo.createCity(invalidPopulation).unsafeRunSync().left.value shouldBe EntryCheckFailed
  }

  it should "not take place for a city with existing name in the same country" in {
    val existingCountryId = originalCities.head.countryId
    val existingCityName = originalCities.head.name
    val cityWithExistingName = newCity.copy(name = existingCityName, countryId = existingCountryId)
    repo.createCity(cityWithExistingName).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }

  it should "throw a foreign key constraint violation for a city with a non-existing country" in {
    val cityWithNonExistingCountry = newCity.copy(countryId = idNotPresent)
    repo
      .createCity(cityWithNonExistingCountry)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  it should "create a new city if all criteria are satisfied" in {
    val newCityId = repo.createCity(newCity).unsafeRunSync().value.value
    val newCityFromDb = repo.getCity(newCityId).unsafeRunSync().value.value

    newCityFromDb shouldBe City.fromCreate(newCityId, newCity)
  }

  it should "throw a conflict error if we create the same city again" in {
    repo.createCity(newCity).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }
}
