package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.airport.AirportPatch
import flightdatabase.domain.country.Country
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.utils.FieldValue
import org.scalatest.Inspectors.forAll

final class AirportRepositoryIT extends RepositoryCheck {

  lazy val repo: AirportRepository[IO] = AirportRepository.make[IO].unsafeRunSync()

  val originalAirports: List[Airport] = List(
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

  val cityToIdMap: Map[String, Long] = Map("Frankfurt am Main" -> 2, "Bangalore" -> 1, "Dubai" -> 4)

  val countryToCityIdMap: Map[String, Long] =
    Map("Germany" -> 2, "India" -> 1, "United Arab Emirates" -> 4)
  val idNotPresent: Long = 10
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  val newAirport: AirportCreate = AirportCreate(
    "Chhatrapati Shivaji Maharaj International Airport",
    "VABB",
    "BOM",
    1,
    2,
    2,
    50000000,
    international = true,
    junction = false
  )

  val updatedName: String = "Chhatrapati Shivaji Maharaj International Airport Updated"
  val patchedName: String = "Chhatrapati Shivaji Maharaj International Airport Patched"

  "Checking if an airport exists" should "return a valid result" in {
    def airportExists(id: Long): Boolean = repo.doesAirportExist(id).unsafeRunSync()
    airportExists(1) shouldBe true
    airportExists(idNotPresent) shouldBe false
    airportExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airports" should "return the correct detailed list" in {
    val airports = repo.getAirports.unsafeRunSync().value.value

    airports should not be empty
    airports should contain only (originalAirports: _*)
  }

  it should "return only names if so required" in {
    val airportsOnlyNames = repo.getAirportsOnlyNames.unsafeRunSync().value.value
    airportsOnlyNames should not be empty
    airportsOnlyNames should contain only (originalAirports.map(_.name): _*)
  }

  "Selecting an airport by id" should "return the correct entry" in {
    def airportById(id: Long): ApiResult[Airport] = repo.getAirport(id).unsafeRunSync()

    forAll(originalAirports)(airport => airportById(airport.id).value.value shouldBe airport)
    airportById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent)
    airportById(veryLongIdNotPresent).left.value shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting airports by other fields" should "return the corresponding entries" in {
    def airportByName(name: String): ApiResult[List[Airport]] =
      repo.getAirports("name", name).unsafeRunSync()
    def airportByIcao(icao: String): ApiResult[List[Airport]] =
      repo.getAirports("icao", icao).unsafeRunSync()
    def airportByIata(iata: String): ApiResult[List[Airport]] =
      repo.getAirports("iata", iata).unsafeRunSync()
    def airportByCityId(id: Long): ApiResult[List[Airport]] =
      repo.getAirports("city_id", id).unsafeRunSync()

    val distinctCityIds = originalAirports.map(_.cityId).distinct

    forAll(originalAirports) { airport =>
      airportByName(airport.name).value.value should contain only airport
      airportByIata(airport.iata).value.value should contain only airport
      airportByIcao(airport.icao).value.value should contain only airport
    }

    forAll(distinctCityIds) { id =>
      val expectedAirports = originalAirports.filter(_.cityId == id)
      airportByCityId(id).value.value should contain only (expectedAirports: _*)
    }

    airportByName(valueNotPresent).left.value shouldBe EntryListEmpty
    airportByIata(valueNotPresent).left.value shouldBe EntryListEmpty
    airportByIcao(valueNotPresent).left.value shouldBe EntryListEmpty
    airportByCityId(idNotPresent).left.value shouldBe EntryListEmpty
    airportByCityId(veryLongIdNotPresent).left.value shouldBe EntryListEmpty
  }

  "Selecting airports by city name" should "return the corresponding entries" in {
    def airportByCity(city: String): ApiResult[List[Airport]] =
      repo.getAirportsByCity(city).unsafeRunSync()

    forAll(cityToIdMap) {
      case (city, id) =>
        airportByCity(city).value.value should contain only (
          originalAirports.filter(_.cityId == id): _*
        )
    }

    airportByCity(valueNotPresent).left.value shouldBe EntryListEmpty
  }

  "Selecting airport by country" should "also return the corresponding entries" in {
    def airportByCountry(country: String): ApiResult[List[Airport]] =
      repo.getAirportsByCountry(country).unsafeRunSync()

    forAll(countryToCityIdMap) {
      case (country, cityId) =>
        airportByCountry(country).value.value should contain only (
          originalAirports.filter(_.cityId == cityId): _*
        )
    }

    val fv = FieldValue[Country, String]("name", valueNotPresent)
    airportByCountry(valueNotPresent).left.value shouldBe EntryNotFound(fv)
  }

  "Creating a new airport" should "not take place if the fields do not satisfy their criteria" in {
    val invalidAirports = List(
      newAirport.copy(name = ""),
      newAirport.copy(icao = ""),
      newAirport.copy(iata = ""),
      newAirport.copy(capacity = -1),
      newAirport.copy(numRunways = -1),
      newAirport.copy(numTerminals = -1)
    )

    forAll(invalidAirports) { invalid =>
      repo.createAirport(invalid).unsafeRunSync().left.value shouldBe EntryCheckFailed
    }
  }

  it should "not take place for an airport with an existing IATA/ICAO code" in {
    val duplicateAirports = List(
      newAirport.copy(iata = originalAirports.head.iata),
      newAirport.copy(icao = originalAirports.head.icao)
    )

    forAll(duplicateAirports) { duplicate =>
      repo.createAirport(duplicate).unsafeRunSync().left.value shouldBe EntryAlreadyExists
    }
  }

  it should "not take place for an airport with an existing name in the same city" in {
    val existingCityId = originalAirports.head.cityId
    val existingName = originalAirports.head.name
    repo
      .createAirport(newAirport.copy(cityId = existingCityId, name = existingName))
      .unsafeRunSync()
      .left
      .value shouldBe EntryAlreadyExists
  }

  it should "throw a foreign key error if the city does not exist" in {
    repo
      .createAirport(newAirport.copy(cityId = idNotPresent))
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  it should "otherwise return the correct id" in {
    val newId = repo.createAirport(newAirport).unsafeRunSync().value.value
    val airports = repo.getAirports.unsafeRunSync().value.value

    airports should contain(Airport.fromCreate(newId, newAirport))
  }

  it should "throw a conflict error if we try to create the same airport again" in {
    repo.createAirport(newAirport).unsafeRunSync().left.value shouldBe EntryAlreadyExists
  }

  "Updating an airport" should "work and return the updated airport ID" in {
    val original = repo.getAirports("name", newAirport.name).unsafeRunSync().value.value.head
    val updated = original.copy(capacity = original.capacity + 100)
    repo.updateAirport(updated).unsafeRunSync().value.value shouldBe updated.id
  }

  it should "also allow changing the name field to a new non-empty value" in {
    val original = repo.getAirports("name", newAirport.name).unsafeRunSync().value.value.head
    val updated = original.copy(name = updatedName)
    repo.updateAirport(updated).unsafeRunSync().value.value shouldBe updated.id

    repo
      .updateAirport(updated.copy(name = ""))
      .unsafeRunSync()
      .left
      .value shouldBe EntryCheckFailed
  }

  it should "throw an error if we update a non-existing airport" in {
    val updated = Airport.fromCreate(idNotPresent, newAirport)
    repo.updateAirport(updated).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the city does not exist" in {
    val updated = Airport.fromCreate(originalAirports.head.id, newAirport)
    repo
      .updateAirport(updated.copy(icao = "XXXX", iata = "XXX", cityId = idNotPresent))
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  "Patching an airport" should "work and return the updated airport" in {
    val original = repo.getAirports("name", updatedName).unsafeRunSync().value.value.head
    val patch = AirportPatch(name = Some(patchedName))
    val patched = Airport.fromPatch(original.id, patch, original)
    repo.partiallyUpdateAirport(original.id, patch).unsafeRunSync().value.value shouldBe patched
  }

  it should "throw an error if we patch a non-existing airport" in {
    val patch = AirportPatch(name = Some("Something"))
    repo
      .partiallyUpdateAirport(idNotPresent, patch)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the city does not exist" in {
    val original = repo.getAirports("name", patchedName).unsafeRunSync().value.value.head
    val patch = AirportPatch(cityId = Some(idNotPresent))
    repo
      .partiallyUpdateAirport(original.id, patch)
      .unsafeRunSync()
      .left
      .value shouldBe EntryHasInvalidForeignKey
  }

  "Removing an airplane" should "work correctly" in {
    val original = repo.getAirports("name", patchedName).unsafeRunSync().value.value.head
    repo.removeAirport(original.id).unsafeRunSync().value.value shouldBe ()
    repo.doesAirportExist(original.id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing airport" in {
    repo.removeAirport(idNotPresent).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }
}
