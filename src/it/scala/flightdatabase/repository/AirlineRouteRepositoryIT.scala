package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryHasInvalidForeignKey
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteCreate
import flightdatabase.domain.airline_route.AirlineRoutePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits.enrichIOOperation
import org.scalatest.Inspectors.forAll

final class AirlineRouteRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineRouteRepository[IO] = AirlineRouteRepository.make[IO].unsafeRunSync()

  val originalAirlineRoutes: List[AirlineRoute] = List(
    AirlineRoute(1, 1, "LH754", 1, 2),
    AirlineRoute(2, 1, "LH755", 2, 1),
    AirlineRoute(3, 5, "EK565", 2, 3),
    AirlineRoute(4, 5, "EK566", 3, 2),
    AirlineRoute(5, 4, "EK47", 3, 1),
    AirlineRoute(6, 4, "EK46", 1, 3)
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "NotPresent"
  val veryLongIdNotPresent: Long = 1039495454540034858L

  // airline-id -> (airline_airplane_ids, airline_name, airline_iata, airline_icao)
  val airlineIdMap: Map[Long, (List[Long], String, String, String)] = Map(
    1L -> (List(1L), "Lufthansa", "LH", "DLH"),
    2L -> (List(4L, 5L), "Emirates", "EK", "UAE")
  )

  // airline-airplane-id -> (airplane_id, airplane_name)
  val airplaneIdMap: Map[Long, (Long, String)] = Map(
    1L -> (2, "747-8"),
    4L -> (1, "A380"),
    5L -> (3, "A320neo")
  )

  // airport-id -> (airport_iata, airport_icao)
  val airportIdMap: Map[Long, (String, String)] = Map(
    1L -> ("FRA", "EDDF"),
    2L -> ("BLR", "VOBL"),
    3L -> ("DXB", "OMDB")
  )

  val newAirlineRoute: AirlineRouteCreate = AirlineRouteCreate(4, "EK48", 3, 1)
  val updatedRouteNumber: String = "EK49"
  val patchedRouteNumber: String = "EK50"

  "Checking if airline-route exists" should "return a valid result" in {
    def airlineRouteExists(id: Long): Boolean = repo.doesAirlineRouteExist(id).unsafeRunSync()
    forAll(originalAirlineRoutes.map(_.id))(airlineRouteExists(_) shouldBe true)
    airlineRouteExists(idNotPresent) shouldBe false
    airlineRouteExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all airline routes" should "return the correct detailed list" in {
    val airlineRoutes = repo.getAirlineRoutes.value

    airlineRoutes should not be empty
    airlineRoutes should contain only (originalAirlineRoutes: _*)
  }

  "Selecting all airline routes with only route numbers" should "return the correct list" in {
    val routeNumbers = repo.getAirlineRoutesOnlyRoutes.value

    routeNumbers should not be empty
    routeNumbers should contain only (originalAirlineRoutes.map(_.route): _*)
  }

  "Selecting an airline route by ID" should "return the correct route" in {
    forAll(originalAirlineRoutes) { airlineRoute =>
      repo.getAirlineRoute(airlineRoute.id).value shouldBe airlineRoute
    }
    repo.getAirlineRoute(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirlineRoute(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airline-route by other fields" should "return the corresponding entries" in {
    def routesByNumber(routeNr: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutes("route_number", routeNr)

    def routeByAirlineAirplaneId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutes("airline_airplane_id", id)

    def routeByStartAirportId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutes("start_airport_id", id)

    def routeByDestinationAirportId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutes("destination_airport_id", id)

    val distinctAirlineAirplaneIds = originalAirlineRoutes.map(_.airlineAirplaneId).distinct
    val distinctStartAirportIds = originalAirlineRoutes.map(_.start).distinct
    val distinctDestinationAirportIds = originalAirlineRoutes.map(_.destination).distinct

    forAll(originalAirlineRoutes)(r => routesByNumber(r.route).value should contain only r)
    forAll(distinctAirlineAirplaneIds) { id =>
      routeByAirlineAirplaneId(id).value should contain only (
        originalAirlineRoutes.filter(_.airlineAirplaneId == id): _*
      )
    }

    forAll(distinctStartAirportIds) { id =>
      routeByStartAirportId(id).value should contain only (
        originalAirlineRoutes.filter(_.start == id): _*
      )
    }

    forAll(distinctDestinationAirportIds) { id =>
      routeByDestinationAirportId(id).value should contain only (
        originalAirlineRoutes.filter(_.destination == id): _*
      )
    }

    routesByNumber(valueNotPresent).error shouldBe EntryListEmpty
    routeByAirlineAirplaneId(idNotPresent).error shouldBe EntryListEmpty
    routeByAirlineAirplaneId(veryLongIdNotPresent).error shouldBe EntryListEmpty
    routeByStartAirportId(idNotPresent).error shouldBe EntryListEmpty
    routeByStartAirportId(veryLongIdNotPresent).error shouldBe EntryListEmpty
    routeByDestinationAirportId(idNotPresent).error shouldBe EntryListEmpty
    routeByDestinationAirportId(veryLongIdNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline-routes by external airline fields" should "return the corresponding entries" in {
    def routesByAirlineId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirlineId(id)

    def routesByAirlineName(name: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline("name", name)

    def routesByAirlineIata(iata: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline("iata", iata)

    def routesByAirlineIcao(icao: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline("icao", icao)

    forAll(airlineIdMap) {
      case (airlineId, (ids, name, iata, icao)) =>
        val expectedRoutes = originalAirlineRoutes.filter(r => ids.contains(r.airlineAirplaneId))

        routesByAirlineId(airlineId).value should contain only (expectedRoutes: _*)
        routesByAirlineName(name).value should contain only (expectedRoutes: _*)
        routesByAirlineIata(iata).value should contain only (expectedRoutes: _*)
        routesByAirlineIcao(icao).value should contain only (expectedRoutes: _*)
    }

    routesByAirlineId(idNotPresent).error shouldBe EntryListEmpty
    routesByAirlineName(valueNotPresent).error shouldBe EntryListEmpty
    routesByAirlineIata(valueNotPresent).error shouldBe EntryListEmpty
    routesByAirlineIcao(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline-routes by external airplane fields" should "return the corresponding entries" in {
    def routesByAirplaneId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirplaneId(id)

    def routesByAirplaneName(name: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirplane("name", name)

    forAll(airplaneIdMap) {
      case (id, (airplaneId, name)) =>
        val expectedRoutes = originalAirlineRoutes.filter(_.airlineAirplaneId == id)

        routesByAirplaneId(airplaneId).value should contain only (expectedRoutes: _*)
        routesByAirplaneName(name).value should contain only (expectedRoutes: _*)
    }

    routesByAirplaneId(idNotPresent).error shouldBe EntryListEmpty
    routesByAirplaneName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting airline-routes by external airport fields" should "return the corresponding entries" in {
    def allRoutesByAirportId(id: Long): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport("id", id, None)

    def inboundRoutesByAirportIata(iata: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport("iata", iata, Some(true))

    def outboundRoutesByAirportIcao(icao: String): IO[ApiResult[List[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport("icao", icao, Some(false))

    forAll(airportIdMap) {
      case (id, (iata, icao)) =>
        val expectedRoutes = originalAirlineRoutes.filter(r => r.start == id || r.destination == id)

        allRoutesByAirportId(id).value should contain only (expectedRoutes: _*)
        inboundRoutesByAirportIata(iata).value should contain only (
          expectedRoutes.filter(_.destination == id): _*
        )
        outboundRoutesByAirportIcao(icao).value should contain only (
          expectedRoutes.filter(_.start == id): _*
        )
    }

    allRoutesByAirportId(idNotPresent).error shouldBe EntryListEmpty
    inboundRoutesByAirportIata(valueNotPresent).error shouldBe EntryListEmpty
    outboundRoutesByAirportIcao(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Creating an airline-route" should "not take place if fields do not satisfy their criteria" in {
    val invalidAirlineRoutes = List(
      newAirlineRoute.copy(airlineAirplaneId = idNotPresent),
      newAirlineRoute.copy(start = idNotPresent),
      newAirlineRoute.copy(destination = idNotPresent)
    )

    forAll(invalidAirlineRoutes) { route =>
      repo.createAirlineRoute(route).error shouldBe EntryHasInvalidForeignKey
    }

    val invalidRoute = newAirlineRoute.copy(route = "")
    repo.createAirlineRoute(invalidRoute).error shouldBe EntryCheckFailed
  }

  it should "not take place if the route number already exists" in {
    val newAirlineRouteWithExistingRouteNumber =
      newAirlineRoute.copy(route = originalAirlineRoutes.head.route)
    repo
      .createAirlineRoute(newAirlineRouteWithExistingRouteNumber)
      .error shouldBe EntryAlreadyExists
  }

  it should "work correctly if all fields are valid" in {
    val newId = repo.createAirlineRoute(newAirlineRoute).value
    val newFromDb = repo.getAirlineRoute(newId).value
    newFromDb shouldBe AirlineRoute.fromCreate(newId, newAirlineRoute)
  }

  it should "throw a conflict error if we create the same route again" in {
    repo.createAirlineRoute(newAirlineRoute).error shouldBe EntryAlreadyExists
  }

  "Updating an airline-route" should "not work if fields do not satisfy their criteria" in {
    val existingRoute = originalAirlineRoutes.head

    val invalidAirlineRoutes = List(
      existingRoute.copy(airlineAirplaneId = idNotPresent),
      existingRoute.copy(start = idNotPresent),
      existingRoute.copy(destination = idNotPresent)
    )

    forAll(invalidAirlineRoutes) { route =>
      repo.updateAirlineRoute(route).error shouldBe EntryHasInvalidForeignKey
    }

    val invalidRoute = existingRoute.copy(route = "")
    repo.updateAirlineRoute(invalidRoute).error shouldBe EntryCheckFailed
  }

  it should "not work if the corresponding airline-route ID does not exist" in {
    val updatedRoute = originalAirlineRoutes.head.copy(id = idNotPresent)
    repo.updateAirlineRoute(updatedRoute).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "not work if the route number already exists" in {
    val existingRoute = originalAirlineRoutes.head
    val updatedRoute = existingRoute.copy(route = newAirlineRoute.route)

    repo.updateAirlineRoute(updatedRoute).error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are satisfied" in {
    val existingRoute = repo.getAirlineRoutes("route_number", newAirlineRoute.route).value.head
    val updatedRoute = existingRoute.copy(route = updatedRouteNumber)
    repo.updateAirlineRoute(updatedRoute).value shouldBe updatedRoute.id
  }

  "Patching an airline-route" should "not work if fields do not satisfy their criteria" in {
    val existingRouteId = originalAirlineRoutes.head.id

    val invalidPatches = List(
      AirlineRoutePatch(airlineAirplaneId = Some(idNotPresent)),
      AirlineRoutePatch(start = Some(idNotPresent)),
      AirlineRoutePatch(destination = Some(idNotPresent))
    )

    forAll(invalidPatches) { patch =>
      repo
        .partiallyUpdateAirlineRoute(existingRouteId, patch)
        .error shouldBe EntryHasInvalidForeignKey
    }
  }

  it should "not work if the corresponding airline-route ID does not exist" in {
    val patch = AirlineRoutePatch(route = Some(patchedRouteNumber))
    repo.partiallyUpdateAirlineRoute(idNotPresent, patch).error shouldBe EntryNotFound(
      idNotPresent
    )
  }

  it should "not work if the route number already exists" in {
    val existingRouteId = originalAirlineRoutes.head.id
    val patch = AirlineRoutePatch(route = Some(updatedRouteNumber))
    repo.partiallyUpdateAirlineRoute(existingRouteId, patch).error shouldBe EntryAlreadyExists
  }

  it should "work if all criteria are satisfied" in {
    val existingRoute = repo.getAirlineRoutes("route_number", updatedRouteNumber).value.head
    val patch = AirlineRoutePatch(route = Some(patchedRouteNumber))
    val patched = existingRoute.copy(route = patchedRouteNumber)
    repo.partiallyUpdateAirlineRoute(existingRoute.id, patch).value shouldBe patched

    val patchedRoute = repo.getAirlineRoute(existingRoute.id).value
    patchedRoute shouldBe patched
  }

  "Removing an airline-route" should "work correctly" in {
    val existingRoute = repo.getAirlineRoutes("route_number", patchedRouteNumber).value.head
    repo.removeAirlineRoute(existingRoute.id).value shouldBe ()
  }

  it should "throw an error if we remove a non-existing airline-route" in {
    repo.removeAirlineRoute(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
