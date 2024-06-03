package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityCreate
import flightdatabase.domain.airline_city.AirlineCityPatch
import flightdatabase.itutils.RepositoryCheck
import flightdatabase.itutils.implicits._
import flightdatabase.utils.implicits.iterableToRichIterable
import org.scalatest.Inspectors.forAll

final class AirlineCityRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineCityRepository[IO] = AirlineCityRepository.make[IO].unsafeRunSync()

  val originalAirlineCities: Nel[AirlineCity] = Nel.of(
    AirlineCity(1, 1, 2),
    AirlineCity(2, 2, 4)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not Present"
  val veryLongIdNotPresent: Long = 1000000000000000000L

  // airlineId -> (name, iata, icao)
  val airlineIdMap: Map[Long, (String, String, String)] = Map(
    1L -> ("Lufthansa", "LH", "DLH"),
    2L -> ("Emirates", "EK", "UAE")
  )

  // cityId -> (name, population)
  val cityIdMap: Map[Long, (String, Long)] =
    Map(2L -> ("Frankfurt am Main", 791000L), 4L -> ("Dubai", 3490000L))

  val newAirlineCity: AirlineCityCreate = AirlineCityCreate(1, 3) // Lufthansa -> Berlin
  val updatedCity: Long = 5                                       // Leiden
  val patchedCity: Long = 3                                       // Back to Berlin

  "Checking if an airline-city exists" should "return a valid result" in {
    def airlineCityExists(id: Long): Boolean = repo.doesAirlineCityExist(id).unsafeRunSync()
    forAll(originalAirlineCities.map(_.id))(airlineCityExists(_) shouldBe true)
    airlineCityExists(idNotPresent) shouldBe false
    airlineCityExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airline-city combinations" should "return the correct detailed list" in {
    val airlineCities = repo.getAirlineCities.value
    airlineCities should contain only (originalAirlineCities.toList: _*)
  }

  "Selecting an airline-city by ID" should "return the correct detailed entry" in {
    forAll(originalAirlineCities) { airlineCity =>
      repo.getAirlineCity(airlineCity.id).value shouldBe airlineCity
    }
    repo.getAirlineCity(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirlineCity(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airline-city by airline and city IDs" should "return the correct detailed entry" in {
    def f(airlineId: Long, cityId: Long): IO[ApiResult[AirlineCity]] =
      repo.getAirlineCity(airlineId, cityId)

    forAll(originalAirlineCities) { airlineCity =>
      f(airlineCity.airlineId, airlineCity.cityId).value shouldBe airlineCity
    }
    f(idNotPresent, 1).error shouldBe EntryListEmpty
    f(1, veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline-city combinations by other fields" should "return the corresponding entries" in {
    def entriesByAirlineId(id: Long): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesBy("airline_id", Nel.one(id), Operator.Equals)
    def entriesByCityId(id: Long): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesBy("city_id", Nel.one(id), Operator.Equals)

    val distinctAirlineIds = originalAirlineCities.map(_.airlineId).distinct
    val distinctCityIds = originalAirlineCities.map(_.cityId).distinct

    forAll(distinctAirlineIds) { airlineId =>
      entriesByAirlineId(airlineId).value should contain only (
        originalAirlineCities.filter(_.airlineId == airlineId): _*
      )
    }

    forAll(distinctCityIds) { cityId =>
      entriesByCityId(cityId).value should contain only (
        originalAirlineCities.filter(_.cityId == cityId): _*
      )
    }

    entriesByAirlineId(idNotPresent).error shouldBe EntryListEmpty
    entriesByAirlineId(veryLongIdNotPresent).error shouldBe EntryListEmpty
    entriesByCityId(idNotPresent).error shouldBe EntryListEmpty
    entriesByCityId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline-city combinations by external fields" should "return the corresponding entries" in {
    def entryByAirlineName(name: String): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesByAirline("name", Nel.one(name), Operator.Equals)

    def entryByAirlineIata(iata: String): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesByAirline("iata", Nel.one(iata), Operator.Equals)

    def entryByAirlineIcao(icao: String): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesByAirline("icao", Nel.one(icao), Operator.Equals)

    def entryByCityName(name: String): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesByCity("name", Nel.one(name), Operator.Equals)

    def entryByCityPop(pop: Long): IO[ApiResult[Nel[AirlineCity]]] =
      repo.getAirlineCitiesByCity("population", Nel.one(pop), Operator.LessThanOrEqualTo)

    forAll(airlineIdMap) {
      case (id, (name, iata, icao)) =>
        entryByAirlineName(name).value should contain only (
          originalAirlineCities.filter(_.airlineId == id): _*
        )
        entryByAirlineIata(iata).value should contain only (
          originalAirlineCities.filter(_.airlineId == id): _*
        )
        entryByAirlineIcao(icao).value should contain only (
          originalAirlineCities.filter(_.airlineId == id): _*
        )
    }

    forAll(cityIdMap) {
      case (id, (name, _)) =>
        entryByCityName(name).value should contain only (
          originalAirlineCities.filter(_.cityId == id): _*
        )
    }

    val mostPopulation = 1000000L

    entryByCityPop(mostPopulation).value should contain only (
      originalAirlineCities.filter { ac =>
        cityIdMap
          .withFilter { case (_, (_, pop)) => pop <= mostPopulation }
          .map { case (cityId, _) => cityId }
          .containsElement(ac.cityId)
      }: _*
    )

    entryByAirlineName(valueNotPresent).error shouldBe EntryListEmpty
    entryByAirlineIata(valueNotPresent).error shouldBe EntryListEmpty
    entryByAirlineIcao(valueNotPresent).error shouldBe EntryListEmpty
    entryByCityName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Creating a new airplane-city" should "not work if the airline or city does not exist" in {
    val invalidAirlineCities = List(
      newAirlineCity.copy(airlineId = idNotPresent),
      newAirlineCity.copy(cityId = idNotPresent)
    )

    forAll(invalidAirlineCities) { airlineCity =>
      repo.createAirlineCity(airlineCity).error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the combination of airline and city already exists" in {
    val existingAirlineCity = originalAirlineCities.head
    val created = AirlineCityCreate(existingAirlineCity.airlineId, existingAirlineCity.cityId)
    repo.createAirlineCity(created).error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val newAirlineCityId = repo.createAirlineCity(newAirlineCity).value
    val newAirlineCityFromDb = repo.getAirlineCity(newAirlineCityId).value
    newAirlineCityFromDb shouldBe AirlineCity.fromCreate(newAirlineCityId, newAirlineCity)
  }

  "Updating an airline-city entry" should "not work if the airline or city does not exist" in {
    val existingAirlineCity = originalAirlineCities.head

    val invalidAirlineCities = List(
      existingAirlineCity.copy(airlineId = idNotPresent),
      existingAirlineCity.copy(cityId = idNotPresent)
    )

    forAll(invalidAirlineCities) { airlineCity =>
      repo.updateAirlineCity(airlineCity).error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the airline-city entry does not exist" in {
    val nonExistingAirlineCity = AirlineCity.fromCreate(idNotPresent, newAirlineCity)
    repo.updateAirlineCity(nonExistingAirlineCity).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not work if the combination of airline and city already exists" in {
    val existingAirlineCityId = originalAirlineCities.head.id
    val updated = AirlineCity.fromCreate(existingAirlineCityId, newAirlineCity)
    repo.updateAirlineCity(updated).error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are met" in {
    val existingAirlineCity =
      repo.getAirlineCity(newAirlineCity.airlineId, newAirlineCity.cityId).value
    val updatedAirlineCity = existingAirlineCity.copy(cityId = updatedCity)

    repo.updateAirlineCity(updatedAirlineCity).value shouldBe existingAirlineCity.id
    repo.getAirlineCity(existingAirlineCity.id).value shouldBe updatedAirlineCity
  }

  "Patching an airline-city entry" should "not work if the airline or city does not exist" in {
    val existingAirlineCityId = originalAirlineCities.head.id

    val invalidAirlineCities = List(
      AirlineCityPatch(airlineId = Some(veryLongIdNotPresent)),
      AirlineCityPatch(cityId = Some(idNotPresent))
    )

    forAll(invalidAirlineCities) { patch =>
      repo
        .partiallyUpdateAirlineCity(existingAirlineCityId, patch)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the airline-city entry does not exist" in {
    val nonExistingAirlineCityId = idNotPresent
    val patch = AirlineCityPatch(cityId = Some(patchedCity))
    repo.partiallyUpdateAirlineCity(nonExistingAirlineCityId, patch).error shouldBe EntryNotFound(
      nonExistingAirlineCityId
    )
  }

  it should "not work if the combination of airline and city already exists" in {
    val existingAirlineCity = originalAirlineCities.find(_.cityId == newAirlineCity.cityId)

    existingAirlineCity.foreach { airlineCity =>
      val patch = AirlineCityPatch(cityId = Some(updatedCity))
      repo.partiallyUpdateAirlineCity(airlineCity.id, patch).error shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val existingAirlineCity = repo.getAirlineCity(newAirlineCity.airlineId, updatedCity).value
    val patch = AirlineCityPatch(cityId = Some(patchedCity))
    val patchedAirlineCity =
      AirlineCity.fromPatch(existingAirlineCity.id, patch, existingAirlineCity)

    repo.partiallyUpdateAirlineCity(existingAirlineCity.id, patch).value shouldBe patchedAirlineCity
    repo.getAirlineCity(existingAirlineCity.id).value shouldBe patchedAirlineCity
  }

  "Removing an airline-city entry" should "not work if the entry does not exist" in {
    repo.removeAirlineCity(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.removeAirlineCity(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  it should "work if the entry exists" in {
    val existingAirlineCity = repo.getAirlineCity(newAirlineCity.airlineId, patchedCity).value
    repo.removeAirlineCity(existingAirlineCity.id).value shouldBe ()
    repo.getAirlineCity(existingAirlineCity.id).error shouldBe EntryNotFound(existingAirlineCity.id)
  }
}
