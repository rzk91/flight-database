package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidField
import flightdatabase.domain.InvalidValueType
import flightdatabase.domain.SqlError
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.airport.AirportPatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits._
import org.scalatest.Inspectors.forAll

final class AirportRepositoryIT extends RepositoryCheck {

  lazy val repo: AirportRepository[IO] = AirportRepository.make[IO].unsafeRunSync()

  val originalAirports: Nel[Airport] = Nel.of(
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
  val invalidFieldSyntax: String = "Field with spaces"
  val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1

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
    repo.getAirports.value should contain only (originalAirports.toList: _*)
  }

  it should "return only names if so required" in {
    repo.getAirportsOnlyNames.value should contain only (originalAirports.map(_.name).toList: _*)
  }

  "Selecting an airport by id" should "return the correct entry" in {
    forAll(originalAirports)(airport => repo.getAirport(airport.id).value shouldBe airport)
    repo.getAirport(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirport(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting airports by other fields" should "return the corresponding entries" in {
    def airportByName(name: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("name", Nel.one(name), Operator.Equals)
    def airportByIcao(icao: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("icao", Nel.one(icao), Operator.Equals)
    def airportByIata(iata: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("iata", Nel.one(iata), Operator.Equals)
    def airportByCityId(id: Long): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("city_id", Nel.one(id), Operator.Equals)

    val distinctCityIds = originalAirports.map(_.cityId).distinct

    forAll(originalAirports) { airport =>
      airportByName(airport.name).value should contain only airport
      airportByIata(airport.iata).value should contain only airport
      airportByIcao(airport.icao).value should contain only airport
    }

    forAll(distinctCityIds) { id =>
      val expectedAirports = originalAirports.filter(_.cityId == id)
      airportByCityId(id).value should contain only (expectedAirports: _*)
    }

    airportByName(valueNotPresent).error shouldBe EntryListEmpty
    airportByIata(valueNotPresent).error shouldBe EntryListEmpty
    airportByIcao(valueNotPresent).error shouldBe EntryListEmpty
    airportByCityId(idNotPresent).error shouldBe EntryListEmpty
    airportByCityId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airports by city name" should "return the corresponding entries" in {
    def airportByCity(city: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsByCity("name", Nel.one(city), Operator.Equals)

    forAll(cityToIdMap) {
      case (city, id) =>
        airportByCity(city).value should contain only (
          originalAirports.filter(_.cityId == id): _*
        )
    }

    airportByCity(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airport by country" should "also return the corresponding entries" in {
    def airportByCountry(country: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsByCountry("name", Nel.one(country), Operator.Equals)

    forAll(countryToCityIdMap) {
      case (country, cityId) =>
        airportByCountry(country).value should contain only (
          originalAirports.filter(_.cityId == cityId): _*
        )
    }

    airportByCountry(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getAirportsBy(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirportsByCity(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirportsByCountry(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax

    repo
      .getAirportsBy(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirportsByCity(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirportsByCountry(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getAirportsBy("iata", Nel.one(invalidStringValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getAirportsByCity("population", Nel.one(invalidLongValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getAirportsByCountry("iso2", Nel.one(invalidStringValue), Operator.Equals)
      .error shouldBe InvalidValueType(invalidStringValue.toString)
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
      repo.createAirport(invalid).error shouldBe EntryCheckFailed
    }
  }

  it should "not take place for an airport with an existing IATA/ICAO code" in {
    val duplicateAirports = List(
      newAirport.copy(iata = originalAirports.head.iata),
      newAirport.copy(icao = originalAirports.head.icao)
    )

    forAll(duplicateAirports) { duplicate =>
      repo.createAirport(duplicate).error shouldBe EntryAlreadyExists
    }
  }

  it should "not take place for an airport with an existing name in the same city" in {
    val existingCityId = originalAirports.head.cityId
    val existingName = originalAirports.head.name
    repo
      .createAirport(newAirport.copy(cityId = existingCityId, name = existingName))
      .error shouldBe EntryAlreadyExists
  }

  it should "throw a foreign key error if the city does not exist" in {
    repo
      .createAirport(newAirport.copy(cityId = idNotPresent))
      .error shouldBe EntryHasInvalidForeignKey
  }

  it should "otherwise return the correct id" in {
    val newId = repo.createAirport(newAirport).value
    val airports = repo.getAirports.value

    airports should contain(Airport.fromCreate(newId, newAirport))
  }

  it should "throw a conflict error if we try to create the same airport again" in {
    repo.createAirport(newAirport).error shouldBe EntryAlreadyExists
  }

  "Updating an airport" should "work and return the updated airport ID" in {
    val original = repo.getAirportsBy("name", Nel.one(newAirport.name), Operator.Equals).value.head
    val updated = original.copy(capacity = original.capacity + 100)
    repo.updateAirport(updated).value shouldBe updated.id
  }

  it should "also allow changing the name field to a new non-empty value" in {
    val original = repo.getAirportsBy("name", Nel.one(newAirport.name), Operator.Equals).value.head
    val updated = original.copy(name = updatedName)
    repo.updateAirport(updated).value shouldBe updated.id

    repo
      .updateAirport(updated.copy(name = ""))
      .error shouldBe EntryCheckFailed
  }

  it should "throw an error if we update a non-existing airport" in {
    val updated = Airport.fromCreate(idNotPresent, newAirport)
    repo.updateAirport(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the city does not exist" in {
    val updated = Airport.fromCreate(originalAirports.head.id, newAirport)
    repo
      .updateAirport(updated.copy(icao = "XXXX", iata = "XXX", cityId = idNotPresent))
      .error shouldBe EntryHasInvalidForeignKey
  }

  "Patching an airport" should "work and return the updated airport" in {
    val original = repo.getAirportsBy("name", Nel.one(updatedName), Operator.Equals).value.head
    val patch = AirportPatch(name = Some(patchedName))
    val patched = Airport.fromPatch(original.id, patch, original)
    repo.partiallyUpdateAirport(original.id, patch).value shouldBe patched
  }

  it should "throw an error if we patch a non-existing airport" in {
    val patch = AirportPatch(name = Some("Something"))
    repo
      .partiallyUpdateAirport(idNotPresent, patch)
      .error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw a foreign key error if the city does not exist" in {
    val original = repo.getAirportsBy("name", Nel.one(patchedName), Operator.Equals).value.head
    val patch = AirportPatch(cityId = Some(idNotPresent))
    repo
      .partiallyUpdateAirport(original.id, patch)
      .error shouldBe EntryHasInvalidForeignKey
  }

  "Removing an airplane" should "work correctly" in {
    val original = repo.getAirportsBy("name", Nel.one(patchedName), Operator.Equals).value.head
    repo.removeAirport(original.id).value shouldBe ()
    repo.doesAirportExist(original.id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing airport" in {
    repo.removeAirport(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
