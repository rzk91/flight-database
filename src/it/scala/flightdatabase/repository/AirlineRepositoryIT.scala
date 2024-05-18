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
import flightdatabase.domain.ValidatedSortAndLimit
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.airline.AirlinePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits._
import org.scalatest.Inspectors.forAll

final class AirlineRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineRepository[IO] = AirlineRepository.make[IO].unsafeRunSync()

  val originalAirlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1039495454540034858L
  val invalidFieldSyntax: String = "Field with spaces"
  val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1
  val emptySortAndLimit: ValidatedSortAndLimit = ValidatedSortAndLimit.empty

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
    repo.getAirlines(emptySortAndLimit).value should contain only (originalAirlines.toList: _*)
  }

  it should "return a properly sorted list" in {
    val sortedAirlines = repo.getAirlines(ValidatedSortAndLimit.sortAscending("name")).value
    sortedAirlines shouldBe originalAirlines.sortBy(_.name)

    val sortedAirlinesDesc = repo.getAirlines(ValidatedSortAndLimit.sortDescending("name")).value
    sortedAirlinesDesc shouldBe originalAirlines.sortBy(_.name).reverse
  }

  it should "return only as many airlines as requested" in {
    val limitedAirlines = repo.getAirlines(ValidatedSortAndLimit.limit(1)).value

    limitedAirlines should have size 1
    limitedAirlines should contain only originalAirlines.head

    val limitedAirlinesWithOffset =
      repo.getAirlines(ValidatedSortAndLimit.limitAndOffset(1, 1)).value

    limitedAirlinesWithOffset should have size 1
    limitedAirlinesWithOffset should contain only originalAirlines.tail.head
  }

  it should "only return the requested fields if so required" in {
    val airlineNames = repo.getAirlinesOnly[String](emptySortAndLimit, "name").value
    airlineNames should contain only (originalAirlines.map(_.name).toList: _*)

    val airlineCountryIds = repo.getAirlinesOnly[Int](emptySortAndLimit, "country_id").value
    airlineCountryIds should contain only (originalAirlines.map(_.countryId).toList: _*)
  }

  it should "sort and return the requested fields if so required" in {
    val airlineNames =
      repo.getAirlinesOnly[String](ValidatedSortAndLimit.sortAscending("name"), "name").value
    airlineNames shouldBe originalAirlines.map(_.name).sorted

    val airlineCountryIds = repo
      .getAirlinesOnly[Int](ValidatedSortAndLimit.sortDescending("country_id"), "country_id")
      .value
    airlineCountryIds shouldBe originalAirlines.map(_.countryId).sorted.reverse

    val airlineNamesCountrySort =
      repo.getAirlinesOnly[String](ValidatedSortAndLimit.sortAscending("country_id"), "name").value

    airlineNamesCountrySort shouldBe originalAirlines.sortBy(_.countryId).map(_.name)
  }

  it should "return an empty list if offset is too large" in {
    val offsetTooLarge = repo.getAirlines(ValidatedSortAndLimit.offset(100)).error
    offsetTooLarge shouldBe EntryListEmpty
  }

  "Selecting an airline by ID" should "return the correct airline" in {
    forAll(originalAirlines)(airplane => repo.getAirline(airplane.id).value shouldBe airplane)
    repo.getAirline(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirline(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airline by other fields" should "return the corresponding airlines" in {
    def airlinesByName(name: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesBy("name", Nel.one(name), Operator.Equals, emptySortAndLimit)
    def airlinesByIata(iata: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesBy("iata", Nel.one(iata), Operator.Equals, emptySortAndLimit)
    def airlinesByIcao(icao: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesBy("icao", Nel.one(icao), Operator.Equals, emptySortAndLimit)
    def airlinesByCallSign(callSign: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesBy("call_sign", Nel.one(callSign), Operator.Equals, emptySortAndLimit)
    def airlinesByCountryId(id: Long): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesBy("country_id", Nel.one(id), Operator.Equals, emptySortAndLimit)

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

  it should "also sort and limit the results as per the request" in {
    repo
      .getAirlinesBy(
        "country_id",
        originalAirlines.map(_.countryId).distinct,
        Operator.In,
        ValidatedSortAndLimit.sortAscending("name")
      )
      .value shouldBe originalAirlines.sortBy(_.name)

    val limitedAirlines = repo
      .getAirlinesBy(
        "country_id",
        originalAirlines.map(_.countryId).distinct,
        Operator.In,
        ValidatedSortAndLimit.limitAndOffset(1, 1)
      )
      .value

    limitedAirlines should have size 1
    limitedAirlines should contain only originalAirlines.tail.head
  }

  "Selecting an airline by country field" should "return the corresponding entries" in {
    def airlineByCountryName(name: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesByCountry("name", Nel.one(name), Operator.Equals, emptySortAndLimit)

    def airlineByCountryIso2(iso2: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesByCountry("iso2", Nel.one(iso2), Operator.Equals, emptySortAndLimit)

    def airlineByCountryIso3(iso3: String): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesByCountry("iso3", Nel.one(iso3), Operator.Equals, emptySortAndLimit)

    def airlineByCountryCode(code: Int): IO[ApiResult[Nel[Airline]]] =
      repo.getAirlinesByCountry("country_code", Nel.one(code), Operator.Equals, emptySortAndLimit)

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

  it should "also sort and limit the results as per the request" in {
    repo
      .getAirlinesByCountry(
        "iso2",
        Nel.fromListUnsafe(countryIdMap.values.map(_._2).toList),
        Operator.In,
        ValidatedSortAndLimit.sortDescending("name")
      )
      .value shouldBe originalAirlines.sortBy(_.name).reverse

    val limitedAirlines = repo
      .getAirlinesByCountry(
        "name",
        Nel.fromListUnsafe(countryIdMap.values.map(_._1).toList),
        Operator.In,
        ValidatedSortAndLimit.sortAscending("iata").copy(limit = Some(1))
      )
      .value

    limitedAirlines should have size 1
    limitedAirlines should contain only originalAirlines.sortBy(_.iata).head
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getAirlinesBy(invalidFieldSyntax, Nel.one("value"), Operator.Equals, emptySortAndLimit)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirlinesByCountry(
        invalidFieldSyntax,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getAirlinesBy(invalidFieldColumn, Nel.one("value"), Operator.Equals, emptySortAndLimit)
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirlinesByCountry(
        invalidFieldColumn,
        Nel.one("value"),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getAirlinesBy("country_id", Nel.one(invalidLongValue), Operator.Equals, emptySortAndLimit)
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getAirlinesByCountry(
        "domain_name",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
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
    val existingAirline =
      repo
        .getAirlinesBy("name", Nel.one(newAirline.name), Operator.Equals, emptySortAndLimit)
        .value
        .head
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
    val existingAirline =
      repo
        .getAirlinesBy("name", Nel.one(updatedName), Operator.Equals, emptySortAndLimit)
        .value
        .head
    val patch = AirlinePatch(name = Some(patchedName))
    val patched = existingAirline.copy(name = patchedName)
    repo.partiallyUpdateAirline(existingAirline.id, patch).value shouldBe patched

    val patchedAirline = repo.getAirline(existingAirline.id).value
    patchedAirline shouldBe patched
  }

  "Removing an airline" should "work correctly" in {
    val existingAirline =
      repo
        .getAirlinesBy("name", Nel.one(patchedName), Operator.Equals, emptySortAndLimit)
        .value
        .head
    repo.removeAirline(existingAirline.id).value shouldBe ()
    repo.getAirline(existingAirline.id).error shouldBe EntryNotFound(existingAirline.id)
  }

  it should "throw an error for a non-existing airline" in {
    repo.removeAirline(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
