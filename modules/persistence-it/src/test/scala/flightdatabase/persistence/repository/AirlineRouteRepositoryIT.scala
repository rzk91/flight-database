package flightdatabase.persistence.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.ApiResult
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
import flightdatabase.airline_route.AirlineRoute
import flightdatabase.airline_route.AirlineRouteCreate
import flightdatabase.airline_route.AirlineRoutePatch
import flightdatabase.persistence.itutils.RepositoryCheck
import flightdatabase.test.syntax.all._
import org.scalatest.Inspectors.forAll

final class AirlineRouteRepositoryIT extends RepositoryCheck {

  lazy val repo: AirlineRouteRepository[IO] = AirlineRouteRepository.make[IO].unsafeRunSync()

  val originalAirlineRoutes: Nel[AirlineRoute] = Nel.of(
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
  val invalidFieldSyntax: String = "Field with spaces"
  val invalidFieldColumn: String = "non_existent_field"
  val invalidLongValue: String = "invalid"
  val invalidStringValue: Int = 1

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
    repo.getAirlineRoutes(emptySortAndLimit).value should contain only (
      originalAirlineRoutes.toList: _*
    )
  }

  it should "return a properly sorted list" in {
    val sorted = repo.getAirlineRoutes(ValidatedSortAndLimit.sortAscending("route_number")).value
    sorted shouldBe originalAirlineRoutes.sortBy(_.route)

    val sortedDesc =
      repo.getAirlineRoutes(ValidatedSortAndLimit.sortDescending("route_number")).value
    sortedDesc shouldBe originalAirlineRoutes.sortBy(_.route).reverse
  }

  it should "return only as many entries as requested" in {
    val limited = repo.getAirlineRoutes(ValidatedSortAndLimit.limit(1)).value
    limited should have size 1
    limited should contain only originalAirlineRoutes.head

    val limitedWithOffset =
      repo.getAirlineRoutes(ValidatedSortAndLimit.limitAndOffset(1, 1)).value
    limitedWithOffset should have size 1
    limitedWithOffset should contain only originalAirlineRoutes.tail.head
  }

  it should "return an empty list if offset is too large" in {
    repo.getAirlineRoutes(ValidatedSortAndLimit.offset(100)).error shouldBe EntryListEmpty
  }

  "Selecting all airline routes with only route numbers" should "return the correct list" in {
    repo
      .getAirlineRoutes[String](emptySortAndLimit, "route_number", StringType)
      .value should contain only (
      originalAirlineRoutes.map(_.route).toList: _*
    )

    repo
      .getAirlineRoutes[Long](emptySortAndLimit, "start_airport_id", LongType)
      .value should contain allElementsOf originalAirlineRoutes.map(_.start).toList
  }

  it should "sort and return the requested fields if so required" in {
    val routeNumbers = repo
      .getAirlineRoutes[String](
        ValidatedSortAndLimit.sortAscending("route_number"),
        "route_number",
        StringType
      )
      .value
    routeNumbers shouldBe originalAirlineRoutes.map(_.route).sorted
  }

  "Selecting an airline route by ID" should "return the correct route" in {
    forAll(originalAirlineRoutes) { airlineRoute =>
      repo.getAirlineRoute(airlineRoute.id).value shouldBe airlineRoute
    }
    repo.getAirlineRoute(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getAirlineRoute(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting an airline-route by other fields" should "return the corresponding entries" in {
    def routesByNumber(routeNr: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesBy(
        "route_number",
        Nel.one(routeNr),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def routeByAirlineAirplaneId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesBy(
        "airline_airplane_id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

    def routeByStartAirportId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesBy(
        "start_airport_id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

    def routeByDestinationAirportId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesBy(
        "destination_airport_id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

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

  it should "sort and limit the filtered entries if so required" in {
    // route_number IN (every route number) matches all entries
    val allRouteNumbers = originalAirlineRoutes.map(_.route)

    val sorted = repo
      .getAirlineRoutesBy(
        "route_number",
        allRouteNumbers,
        Operator.In,
        ValidatedSortAndLimit.sortDescending("route_number"),
        StringType
      )
      .value
    sorted shouldBe originalAirlineRoutes.sortBy(_.route).reverse

    val limited = repo
      .getAirlineRoutesBy(
        "route_number",
        allRouteNumbers,
        Operator.In,
        ValidatedSortAndLimit.sortAscending("route_number").copy(limit = Some(1)),
        StringType
      )
      .value
    limited should contain only originalAirlineRoutes.sortBy(_.route).head
  }

  "Selecting airline-routes by external airline fields" should "return the corresponding entries" in {
    def routesByAirlineId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline(
        "id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

    def routesByAirlineName(name: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline(
        "name",
        Nel.one(name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def routesByAirlineIata(iata: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline(
        "iata",
        Nel.one(iata),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def routesByAirlineIcao(icao: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirline(
        "icao",
        Nel.one(icao),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

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
    def routesByAirplaneId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirplane(
        "id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

    def routesByAirplaneName(name: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirplane(
        "name",
        Nel.one(name),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

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
    def allRoutesByAirportId(id: Long): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport(None)(
        "id",
        Nel.one(id),
        Operator.Equals,
        emptySortAndLimit,
        LongType
      )

    def inboundRoutesByAirportIata(iata: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport(Some(true))(
        "iata",
        Nel.one(iata),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

    def outboundRoutesByAirportIcao(icao: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport(Some(false))(
        "icao",
        Nel.one(icao),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )

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

  "Selecting airline-routes by airport with an exclusion operator" should
    "exclude routes across both endpoints (any/none semantics)" in {
    def excludeByAirportId(id: Long, inbound: Option[Boolean]): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport(inbound)(
        "id",
        Nel.one(id),
        Operator.NotIn,
        emptySortAndLimit,
        LongType
      )

    def excludeByAirportIata(iata: String): IO[ApiResult[Nel[AirlineRoute]]] =
      repo.getAirlineRoutesByAirport(None)(
        "iata",
        Nel.one(iata),
        Operator.NotEquals,
        emptySortAndLimit,
        StringType
      )

    forAll(airportIdMap) {
      case (id, (iata, _)) =>
        // None: a route is excluded if the airport is the start OR the destination.
        // Without the fix, the per-leg UNION would wrongly keep routes where the
        // airport is the *other* endpoint.
        val notEitherEndpoint =
          originalAirlineRoutes.filterNot(r => r.start == id || r.destination == id)
        excludeByAirportId(id, None).value should contain only (notEitherEndpoint: _*)
        excludeByAirportIata(iata).value should contain only (notEitherEndpoint: _*)

        // inbound = arrivals -> exclude where the airport is the destination
        excludeByAirportId(id, Some(true)).value should contain only (
          originalAirlineRoutes.filterNot(_.destination == id): _*
        )

        // outbound = departures -> exclude where the airport is the start
        excludeByAirportId(id, Some(false)).value should contain only (
          originalAirlineRoutes.filterNot(_.start == id): _*
        )
    }
  }

  "Selecting a non-existent field" should "return an error" in {
    val nelValue = Nel.one("value")

    repo
      .getAirlineRoutesBy(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getAirlineRoutesByAirline(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getAirlineRoutesByAirplane(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))
    repo
      .getAirlineRoutesByAirport(None)(
        invalidFieldSyntax,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe sqlErrorInvalidSyntax(Some(invalidFieldSyntax), Some(nelValue))

    repo
      .getAirlineRoutesBy(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirlineRoutesByAirline(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirlineRoutesByAirplane(
        invalidFieldColumn,
        nelValue,
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidField(invalidFieldColumn)
    repo
      .getAirlineRoutesByAirport(None)(
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
      .getAirlineRoutesBy(
        "route_number",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getAirlineRoutesByAirline(
        "name",
        Nel.one(invalidStringValue),
        Operator.Equals,
        emptySortAndLimit,
        IntType
      )
      .error shouldBe InvalidValueType(invalidStringValue.toString)
    repo
      .getAirlineRoutesByAirplane(
        "capacity",
        Nel.one(invalidLongValue),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidValueType(invalidLongValue)
    repo
      .getAirlineRoutesByAirport(None)(
        "number_of_runways",
        Nel.one(invalidLongValue),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .error shouldBe InvalidValueType(invalidLongValue)
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
    val existingRoute = repo
      .getAirlineRoutesBy(
        "route_number",
        Nel.one(newAirlineRoute.route),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .value
      .head
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
    val existingRoute = repo
      .getAirlineRoutesBy(
        "route_number",
        Nel.one(updatedRouteNumber),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .value
      .head
    val patch = AirlineRoutePatch(route = Some(patchedRouteNumber))
    val patched = existingRoute.copy(route = patchedRouteNumber)
    repo.partiallyUpdateAirlineRoute(existingRoute.id, patch).value shouldBe patched

    val patchedRoute = repo.getAirlineRoute(existingRoute.id).value
    patchedRoute shouldBe patched
  }

  "Removing an airline-route" should "work correctly" in {
    val existingRoute = repo
      .getAirlineRoutesBy(
        "route_number",
        Nel.one(patchedRouteNumber),
        Operator.Equals,
        emptySortAndLimit,
        StringType
      )
      .value
      .head
    repo.removeAirlineRoute(existingRoute.id).value shouldBe ()
  }

  it should "throw an error if we remove a non-existing airline-route" in {
    repo.removeAirlineRoute(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
