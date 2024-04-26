package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.airline.AirlinePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits.enrichIOOperation
import org.scalatest.Inspectors.forAll

final class AirlineRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineRepository[IO] = AirlineRepository.make[IO].unsafeRunSync()

  val originalAirlines: List[Airline] = List(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  // ID -> (Name, ISO2, ISO3, Country [Phone] Code)
  val countryIdMap: Map[Long, (String, String, String, Int)] = Map(
    2L -> ("Germany", "DE", "DEU", 49),
    4L -> ("United Arab Emirates", "AE", "ARE", 971)
  )

  val newAirline: AirlineCreate = AirlineCreate("Indigo", "6E", "IGO", "IFLY", 1)
  val updatedName: String = "IndiGo"
  val patchedName: String = "IndiGo Airlines"

  val newAirlineInDifferentCountry: AirlineCreate = AirlineCreate("Indigo", "7E", "IDO", "IFLO", 4)

  "Checking if an airline exists" should "return a valid result" in {
    def airlineExists(id: Long): Boolean = repo.doesAirlineExist(id).unsafeRunSync()
    forAll(originalAirlines.map(_.id))(id => airlineExists(id) shouldBe true)
    airlineExists(idNotPresent) shouldBe false
    airlineExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airlines" should "return the correct detailed list" in {
    val airlines = repo.getAirlines.value

    airlines should not be empty
    airlines should contain only (originalAirlines: _*)
  }

  it should "only return names if so required" in {
    val airlineNames = repo.getAirlinesOnlyNames.value
    airlineNames should not be empty
    airlineNames should contain only (originalAirlines.map(_.name): _*)
  }

  "Selecting an airline by ID" should "return the correct airline" in {
    forAll(originalAirlines)(airplane => repo.getAirline(airplane.id).value shouldBe airplane)
    repo.getAirline(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirline(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airline by other fields" should "return the corresponding airlines" in {
    def airlinesByName(name: String): IO[ApiResult[List[Airline]]] = repo.getAirlines("name", name)
    def airlinesByIata(iata: String): IO[ApiResult[List[Airline]]] = repo.getAirlines("iata", iata)
    def airlinesByIcao(icao: String): IO[ApiResult[List[Airline]]] = repo.getAirlines("icao", icao)
    def airlinesByCallSign(callSign: String): IO[ApiResult[List[Airline]]] =
      repo.getAirlines("call_sign", callSign)
    def airlinesByCountryId(id: Long): IO[ApiResult[List[Airline]]] =
      repo.getAirlines("country_id", id)

    val distinctNames = originalAirlines.map(_.name).distinct
    val distinctCountryIds = originalAirlines.map(_.countryId).distinct

    forAll(originalAirlines) { airline =>
      airlinesByIata(airline.iata).value should contain only airline
      airlinesByIcao(airline.icao).value should contain only airline
      airlinesByCallSign(airline.callSign).value should contain only airline
    }

    forAll(distinctNames) { name =>
      airlinesByName(name).value should contain only (originalAirlines.filter(_.name == name): _*)
    }

    forAll(distinctCountryIds) { country =>
      airlinesByCountryId(country).value should contain only (
        originalAirlines.filter(_.countryId == country): _*
      )
    }

    airlinesByName(valueNotPresent).error shouldBe EntryListEmpty
    airlinesByIata(valueNotPresent).error shouldBe EntryListEmpty
    airlinesByIcao(valueNotPresent).error shouldBe EntryListEmpty
    airlinesByCallSign(valueNotPresent).error shouldBe EntryListEmpty
    airlinesByCountryId(idNotPresent).error shouldBe EntryListEmpty
    airlinesByCountryId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting an airline by country field" should "return the corresponding entries" in {
    def airlineByCountryName(name: String): IO[ApiResult[List[Airline]]] =
      repo.getAirlinesByCountry("name", name)

    def airlineByCountryIso2(iso2: String): IO[ApiResult[List[Airline]]] =
      repo.getAirlinesByCountry("iso2", iso2)

    def airlineByCountryIso3(iso3: String): IO[ApiResult[List[Airline]]] =
      repo.getAirlinesByCountry("iso3", iso3)

    def airlineByCountryCode(code: Int): IO[ApiResult[List[Airline]]] =
      repo.getAirlinesByCountry("country_code", code)

    forAll(countryIdMap) {
      case (id, (name, iso2, iso3, code)) =>
        airlineByCountryName(name).value should contain only (
          originalAirlines.filter(_.countryId == id): _*
        )
        airlineByCountryIso2(iso2).value should contain only (
          originalAirlines.filter(_.countryId == id): _*
        )
        airlineByCountryIso3(iso3).value should contain only (
          originalAirlines.filter(_.countryId == id): _*
        )
        airlineByCountryCode(code).value should contain only (
          originalAirlines.filter(_.countryId == id): _*
        )
    }

    airlineByCountryName(valueNotPresent).error shouldBe EntryListEmpty
    airlineByCountryIso2(valueNotPresent).error shouldBe EntryListEmpty
    airlineByCountryIso3(valueNotPresent).error shouldBe EntryListEmpty
    airlineByCountryCode(-1).error shouldBe EntryListEmpty
  }

  "Creating a new airline" should "not take place if fields do not satisfy their criteria" in {
    val invalidAirlines = List(
      newAirline.copy(name = ""),
      newAirline.copy(iata = ""),
      newAirline.copy(icao = ""),
      newAirline.copy(callSign = "")
    )

    forAll(invalidAirlines)(airline => repo.createAirline(airline).error shouldBe EntryCheckFailed)
  }

  it should "not take place if the name exists in the same country" in {
    val existingCountryId = originalAirlines.head.countryId
    val existingAirlineName = originalAirlines.head.name
    val airlineWithExistingName =
      newAirline.copy(countryId = existingCountryId, name = existingAirlineName)
    repo.createAirline(airlineWithExistingName).error shouldBe EntryAlreadyExists
  }

  it should "throw a foreign key constraint violation if the country does not exist" in {
    val airlineWithNonExistentCountry = newAirline.copy(countryId = idNotPresent)
    repo.createAirline(airlineWithNonExistentCountry).error shouldBe EntryHasInvalidForeignKey
  }

  it should "work if all criteria are met" in {
    val newAirlineId = repo.createAirline(newAirline).value
    val newAirlineFromDb = repo.getAirline(newAirlineId).value
    newAirlineFromDb shouldBe Airline.fromCreate(newAirlineId, newAirline)
  }

  it should "throw a conflict error if the airline already exists" in {
    repo.createAirline(newAirline).error shouldBe EntryAlreadyExists
  }

  it should "not throw a conflict error if the airplane with the same name exists in a different country" in {
    val newAirlineId = repo.createAirline(newAirlineInDifferentCountry).value
    val newAirlineFromDb = repo.getAirline(newAirlineId).value
    newAirlineFromDb shouldBe Airline.fromCreate(newAirlineId, newAirlineInDifferentCountry)
  }

  "Updating an airline" should "not take place if fields do not satisfy their criteria" in {
    val existingAirline = originalAirlines.head

    val invalidAirlines = List(
      existingAirline.copy(name = ""),
      existingAirline.copy(iata = ""),
      existingAirline.copy(icao = ""),
      existingAirline.copy(callSign = "")
    )

    forAll(invalidAirlines)(airline => repo.updateAirline(airline).error shouldBe EntryCheckFailed)
  }

  it should "throw an error if we update with an existing unique field" in {
    val existingAirline = originalAirlines.head

    val duplicateAirlines = List(
      existingAirline.copy(name = newAirline.name, countryId = newAirline.countryId),
      existingAirline.copy(iata = newAirline.iata),
      existingAirline.copy(icao = newAirline.icao),
      existingAirline.copy(callSign = newAirline.callSign)
    )

    forAll(duplicateAirlines)(airline =>
      repo.updateAirline(airline).error shouldBe EntryAlreadyExists
    )
  }

  it should "throw an error if we update a non-existing airline" in {
    val updated = Airline.fromCreate(idNotPresent, newAirline)
    repo.updateAirline(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "throw an error if the corresponding country does not exist" in {
    val existingAirline = originalAirlines.head
    val updated = existingAirline.copy(countryId = idNotPresent)
    repo.updateAirline(updated).error shouldBe EntryHasInvalidForeignKey
  }

  it should "work if all criteria are met" in {
    val existingAirline = repo.getAirlines("name", newAirline.name).value.head
    val updated = existingAirline.copy(name = updatedName)
    repo.updateAirline(updated).value shouldBe updated.id

    val updatedAirline = repo.getAirline(existingAirline.id).value
    updatedAirline shouldBe updated
  }

  "Patching an airline" should "not take place if fields do not satisfy their criteria" in {
    val existingAirline = originalAirlines.head

    val invalidAirlines = List(
      AirlinePatch(name = Some("")),
      AirlinePatch(iata = Some("")),
      AirlinePatch(icao = Some("")),
      AirlinePatch(callSign = Some(""))
    )

    forAll(invalidAirlines)(patch =>
      repo.partiallyUpdateAirline(existingAirline.id, patch).error shouldBe EntryCheckFailed
    )
  }

  it should "throw an error for a non-existing airline" in {
    val patch = AirlinePatch(name = Some(patchedName))
    repo.partiallyUpdateAirline(idNotPresent, patch).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not take place for existing unique fields" in {
    val existingAirline = originalAirlines.head

    val duplicatePatches = List(
      AirlinePatch(name = Some(updatedName), countryId = Some(newAirline.countryId)),
      AirlinePatch(iata = Some(newAirline.iata)),
      AirlinePatch(icao = Some(newAirline.icao)),
      AirlinePatch(callSign = Some(newAirline.callSign))
    )

    forAll(duplicatePatches)(patch =>
      repo.partiallyUpdateAirline(existingAirline.id, patch).error shouldBe EntryAlreadyExists
    )
  }

  it should "throw an error if the corresponding country does not exist" in {
    val existingAirline = originalAirlines.head
    val patch = AirlinePatch(countryId = Some(idNotPresent))
    repo.partiallyUpdateAirline(existingAirline.id, patch).error shouldBe EntryHasInvalidForeignKey
  }

  it should "work if all criteria are met" in {
    val existingAirline = repo.getAirlines("name", updatedName).value.head
    val patch = AirlinePatch(name = Some(patchedName))
    val patched = existingAirline.copy(name = patchedName)
    repo.partiallyUpdateAirline(existingAirline.id, patch).value shouldBe patched

    val patchedAirline = repo.getAirline(existingAirline.id).value
    patchedAirline shouldBe patched
  }

  "Removing an airline" should "work correctly" in {
    val existingAirline = repo.getAirlines("name", patchedName).value.head
    repo.removeAirline(existingAirline.id).value shouldBe ()
    repo.getAirline(existingAirline.id).error shouldBe EntryNotFound(existingAirline.id)
  }

  it should "throw an error for a non-existing airline" in {
    repo.removeAirline(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
