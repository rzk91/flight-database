package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidTimezone
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityCreate
import flightdatabase.domain.city.CityPatch
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

  val idNotPresent: Long = 100
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

  val updatedName: String = "München"
  val patchedName: String = "Minga"

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

  "Creating a new city" should "not take place if fields do not satisfy their criteria" in {
    val invalidCity = newCity.copy(name = "")
    repo.createCity(invalidCity).unsafeRunSync().left.value shouldBe EntryCheckFailed

    val invalidTimezone = newCity.copy(timezone = "")
    repo.createCity(invalidTimezone).unsafeRunSync().left.value shouldBe InvalidTimezone("")

    val invalidPopulation = newCity.copy(population = -1)
    repo.createCity(invalidPopulation).unsafeRunSync().left.value shouldBe EntryCheckFailed
  }

  it should "not take place for a city with existing name in the same country" in {
    val existingCountryId = originalCities.head.countryId
    val existingCityName = originalCities.head.name
    val existingCityTz = originalCities.head.timezone
    val cityWithExistingName = newCity.copy(
      name = existingCityName,
      countryId = existingCountryId,
      timezone = existingCityTz
    )
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

  it should "throw an invalid timezone error for a city with a non-existing timezone" in {
    val invalidTimezone = "Invalid/Timezone"
    val cityWithInvalidTimezone = newCity.copy(timezone = invalidTimezone)
    repo.createCity(cityWithInvalidTimezone).unsafeRunSync().left.value shouldBe InvalidTimezone(
      invalidTimezone
    )
  }

  it should "also throw an invalid timezone error if the city's timezone does not match the corresponding country's timezone" in {
    val invalidTimezone = "America/Chicago"
    val cityWithInvalidTimezone =
      newCity.copy(countryId = countryToIdMap("Germany"), timezone = invalidTimezone)
    repo.createCity(cityWithInvalidTimezone).unsafeRunSync().left.value shouldBe InvalidTimezone(
      invalidTimezone
    )
  }

  it should "create a new city if all criteria are satisfied" in {
    val newCityId = repo.createCity(newCity).unsafeRunSync().value.value
    val newCityFromDb = repo.getCity(newCityId).unsafeRunSync().value.value

    newCityFromDb shouldBe City.fromCreate(newCityId, newCity)
  }

  it should "throw a conflict error if we create the same city again" in {
    repo.createCity(newCity).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }

  it should "not throw a conflict error if we create a city with the same name in a different country" in {
    val newCityId = repo.createCity(newBerlinInUSA).unsafeRunSync().value.value
    val newCityFromDb = repo.getCity(newCityId).unsafeRunSync().value.value

    newCityFromDb shouldBe City.fromCreate(newCityId, newBerlinInUSA)
  }

  "Updating a city" should "work and return the updated city ID" in {
    val original = repo.getCities("name", newCity.name).unsafeRunSync().value.value.head
    val updatedCity = original.copy(population = original.population + 100000)
    repo.updateCity(updatedCity).unsafeRunSync().value.value shouldBe updatedCity.id
  }

  it should "also allow changing the city's name to a non-empty value" in {
    val original = repo.getCities("name", newCity.name).unsafeRunSync().value.value.head
    val updatedCity = original.copy(name = updatedName)
    repo.updateCity(updatedCity).unsafeRunSync().value.value shouldBe updatedCity.id

    repo
      .updateCity(updatedCity.copy(name = ""))
      .unsafeRunSync()
      .left
      .value shouldBe EntryCheckFailed
  }

  it should "throw an error if we update a non-existing city" in {
    val updated = City.fromCreate(idNotPresent, newCity)
    repo.updateCity(updated).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw an error if we update the timezone to an invalid value" in {
    val original = repo.getCities("name", updatedName).unsafeRunSync().value.value.head
    val updatedCity = original.copy(timezone = "")
    repo.updateCity(updatedCity).unsafeRunSync().left.value shouldBe InvalidTimezone("")

    val invalidTimezone = "Europe/Munich"
    val updatedCity2 = original.copy(timezone = invalidTimezone)
    repo.updateCity(updatedCity2).unsafeRunSync().left.value shouldBe InvalidTimezone(
      invalidTimezone
    )
  }

  it should "throw a foreign key error if the country does not exist" in {
    val original = repo.getCities("name", updatedName).unsafeRunSync().value.value.head
    val updatedCity = original.copy(countryId = idNotPresent)
    repo.updateCity(updatedCity).unsafeRunSync().left.value shouldBe EntryHasInvalidForeignKey
  }

  "Patching a city" should "work and return the updated city ID" in {
    val original = repo.getCities("name", updatedName).unsafeRunSync().value.value.head
    val patch = CityPatch(name = Some(patchedName))
    val patched = City.fromPatch(original.id, patch, original)
    repo.partiallyUpdateCity(original.id, patch).unsafeRunSync().value.value shouldBe patched
  }

  it should "throw an error if we patch a non-existing city" in {
    val patch = CityPatch(name = Some("Some name"))
    repo.partiallyUpdateCity(idNotPresent, patch).unsafeRunSync().left.value shouldBe EntryNotFound(
      idNotPresent
    )
  }

  it should "throw an error if we patch a city with an invalid timezone" in {
    val original = repo.getCities("name", patchedName).unsafeRunSync().value.value.head
    val patch = CityPatch(timezone = Some(""))
    repo
      .partiallyUpdateCity(original.id, patch)
      .unsafeRunSync()
      .left
      .value shouldBe InvalidTimezone("")

    val invalidTimezone = "Europe/Munich"
    val patch2 = CityPatch(timezone = Some(invalidTimezone))
    repo
      .partiallyUpdateCity(original.id, patch2)
      .unsafeRunSync()
      .left
      .value shouldBe InvalidTimezone(invalidTimezone)
  }

  it should "throw a foreign key error if the country does not exist" in {
    val original = repo.getCities("name", patchedName).unsafeRunSync().value.value.head
    val patch = CityPatch(countryId = Some(idNotPresent))
    repo
      .partiallyUpdateCity(original.id, patch)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  "Removing a city" should "work correctly" in {
    val cityToRemove = repo.getCities("name", patchedName).unsafeRunSync().value.value.head
    repo.removeCity(cityToRemove.id).unsafeRunSync().value.value shouldBe ()
    repo.doesCityExist(cityToRemove.id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing city" in {
    repo.removeCity(idNotPresent).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }
}
