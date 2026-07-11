package flightdatabase.persistence.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.ApiResult
import flightdatabase.BooleanType
import flightdatabase.EntryAlreadyExists
import flightdatabase.EntryCheckFailed
import flightdatabase.EntryHasInvalidForeignKey
import flightdatabase.EntryListEmpty
import flightdatabase.EntryNotFound
import flightdatabase.IntType
import flightdatabase.InvalidField
import flightdatabase.InvalidValueType
import flightdatabase.LongType
import flightdatabase.Operator
import flightdatabase.StringType
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airport.Airport
import flightdatabase.airport.AirportCreate
import flightdatabase.airport.AirportPatch
import flightdatabase.airport.TaxiDuration
import flightdatabase.persistence.itutils.RepositoryCheck
import flightdatabase.syntax.string._
import flightdatabase.test.syntax.all._
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

  val cityToIdMap: Map[String, Long] = Map("Frankfurt am Main" -> 2, "Bangalore" -> 1, "Dubai" -> 4)

  val countryToCityIdMap: Map[String, Long] =
    Map("Germany" -> 2, "India" -> 1, "United Arab Emirates" -> 4)
  val idNotPresent: Long = 10
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L
  val invalidFieldSyntax: String = "Field with spaces"
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
    junction = false,
    latitude = BigDecimal("19.0896"),
    longitude = BigDecimal("72.8656"),
    taxiOutDuration = TaxiDuration(20),
    taxiInDuration = TaxiDuration(10)
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
    repo.getAirports(emptySortAndLimit).value should contain only (originalAirports.toList: _*)
  }

  it should "return a properly sorted list" in {
    val sorted = repo.getAirports(ValidatedSortAndLimit.sortAscending("name")).value
    sorted shouldBe originalAirports.sortBy(_.name)

    val sortedDesc = repo.getAirports(ValidatedSortAndLimit.sortDescending("name")).value
    sortedDesc shouldBe originalAirports.sortBy(_.name).reverse
  }

  it should "return only as many airports as requested" in {
    val limited = repo.getAirports(ValidatedSortAndLimit.limit(1)).value

    limited should have size 1
    limited should contain only originalAirports.head

    val limitedWithOffset = repo.getAirports(ValidatedSortAndLimit.limitAndOffset(1, 1)).value

    limitedWithOffset should have size 1
    limitedWithOffset should contain only originalAirports.tail.head
  }

  it should "only return the requested fields if so required" in {
    repo
      .getAirports[String](emptySortAndLimit, "name", StringType)
      .value should contain only (originalAirports.map(_.name).toList: _*)

    repo
      .getAirports[Boolean](emptySortAndLimit, "international", BooleanType)
      .value should contain allElementsOf originalAirports.map(_.international).toList
  }

  it should "sort and return the requested fields if so required" in {
    val names =
      repo
        .getAirports[String](ValidatedSortAndLimit.sortAscending("name"), "name", StringType)
        .value
    names shouldBe originalAirports.map(_.name).sorted
  }

  it should "return an empty list if offset is too large" in {
    repo.getAirports(ValidatedSortAndLimit.offset(100)).error shouldBe EntryListEmpty
  }

  "Selecting an airport by id" should "return the correct entry" in {
    forAll(originalAirports)(airport => repo.getAirport(airport.id).value shouldBe airport)
    repo.getAirport(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirport(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting airports by other fields" should "return the corresponding entries" in {
    def airportByName(name: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("name", Nel.one(name), Operator.Equals, emptySortAndLimit, StringType)
    def airportByIcao(icao: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("icao", Nel.one(icao), Operator.Equals, emptySortAndLimit, StringType)
    def airportByIata(iata: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("iata", Nel.one(iata), Operator.Equals, emptySortAndLimit, StringType)
    def airportByCityId(id: Long): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("city_id", Nel.one(id), Operator.Equals, emptySortAndLimit, LongType)
    def airportByJunction(junction: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsBy("junction", Nel.one(junction), Operator.Is, emptySortAndLimit, StringType)

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

    forAll(List("TRUE", "False", "Invalid")) { junction =>
      junction.asBoolean match {
        case Some(j) =>
          val expectedAirports = originalAirports.filter(_.junction == j)
          airportByJunction(junction).value should contain only (expectedAirports: _*)
        case None =>
          airportByJunction(junction).error shouldBe sqlErrorInvalidSyntax(
            Some("junction"),
            Some(Nel.one(junction))
          )
      }
    }

    airportByName(valueNotPresent).error shouldBe EntryListEmpty
    airportByIata(valueNotPresent).error shouldBe EntryListEmpty
    airportByIcao(valueNotPresent).error shouldBe EntryListEmpty
    airportByCityId(idNotPresent).error shouldBe EntryListEmpty
    airportByCityId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  it should "sort and limit the filtered entries if so required" in {
    // city_id IN (every airport's city) matches all airports
    val allCityIds = originalAirports.map(_.cityId)

    val sortedByName = repo
      .getAirportsBy(
        "city_id",
        allCityIds,
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name"),
        LongType
      )
      .value
    sortedByName shouldBe originalAirports.sortBy(_.name)

    val limited = repo
      .getAirportsBy(
        "city_id",
        allCityIds,
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1)),
        LongType
      )
      .value
    limited should contain only originalAirports.sortBy(_.name).head
  }

  "Selecting airports by city name" should "return the corresponding entries" in {
    def airportByCity(city: String): IO[ApiResult[Nel[Airport]]] =
      repo.getAirportsByCity("name", Nel.one(city), Operator.Equals, emptySortAndLimit, StringType)

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
      repo.getAirportsByCountry(
        "name",
        Nel.one(country),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    forAll(countryToCityIdMap) {
      case (country, cityId) =>
        airportByCountry(country).value should contain only (
          originalAirports.filter(_.cityId == cityId): _*
        )
    }

    airportByCountry(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Sorting and limiting airports filtered by city/country" should "work if so required" in {
    val allCities = Nel.fromListUnsafe(cityToIdMap.keys.toList)
    val allCountries = Nel.fromListUnsafe(countryToCityIdMap.keys.toList)

    repo
      .getAirportsByCity(
        "name",
        allCities,
        Operator.In,
        ValidatedSortAndLimit.sortDescending("name"),
        StringType
      )
      .value shouldBe originalAirports.sortBy(_.name).reverse

    repo
      .getAirportsByCountry(
        "name",
        allCountries,
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name").copy(limit = Some(1)),
        StringType
      )
      .value should contain only originalAirports.sortBy(_.name).head
  }

  "Selecting a non-existent field" should "return an error" in {
    val nelValue = Nel.one("value")

    repo
      .getAirportsBy(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getAirportsByCity(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getAirportsByCountry(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))

    repo
      .getAirportsBy(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirportsByCity(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirportsByCountry(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getAirportsBy(
        "iata",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getAirportsByCity(
        "population",
        Nel.one(invalidLongValue),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getAirportsByCountry(
        "iso2",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
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
    val airports = repo.getAirports(emptySortAndLimit).value

    airports should contain(Airport.fromCreate(newId, newAirport))
  }

  it should "throw a conflict error if we try to create the same airport again" in {
    repo.createAirport(newAirport).error shouldBe EntryAlreadyExists
  }

  it should "persist distinct coordinates for two airports sharing the same city" in {
    val sameCityId = originalAirports.head.cityId
    val second = newAirport.copy(
      name = "Frankfurt-Hahn Airport",
      icao = "EDFH",
      iata = "HHN",
      cityId = sameCityId,
      latitude = BigDecimal("49.9487"),
      longitude = BigDecimal("7.2639")
    )

    val secondId = repo.createAirport(second).value
    val created = repo.getAirport(secondId).value
    val original = originalAirports.head

    created.cityId shouldBe original.cityId
    (created.latitude, created.longitude) should not be ((original.latitude, original.longitude))
  }

  "Updating an airport" should "work and return the updated airport ID" in {
    val original = repo
      .getAirportsBy(
        "name",
        Nel.one(newAirport.name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .value
      .head
    val updated = original.copy(capacity = original.capacity + 100)
    repo.updateAirport(updated).value shouldBe updated.id
  }

  it should "also allow changing the name field to a new non-empty value" in {
    val original = repo
      .getAirportsBy(
        "name",
        Nel.one(newAirport.name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .value
      .head
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
    val original = repo
      .getAirportsBy("name", Nel.one(updatedName), Operator.Equals, emptySortAndLimit, StringType)
      .value
      .head
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
    val original = repo
      .getAirportsBy("name", Nel.one(patchedName), Operator.Equals, emptySortAndLimit, StringType)
      .value
      .head
    val patch = AirportPatch(cityId = Some(idNotPresent))
    repo
      .partiallyUpdateAirport(original.id, patch)
      .error shouldBe EntryHasInvalidForeignKey
  }

  "Removing an airplane" should "work correctly" in {
    val original = repo
      .getAirportsBy("name", Nel.one(patchedName), Operator.Equals, emptySortAndLimit, StringType)
      .value
      .head
    repo.removeAirport(original.id).value shouldBe ()
    repo.doesAirportExist(original.id).unsafeRunSync() shouldBe false
  }

  it should "throw an error if we try to remove a non-existing airport" in {
    repo.removeAirport(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
