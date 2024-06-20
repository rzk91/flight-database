package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.syntax.foldable._
import doobie.Put
import doobie.Read
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.Got
import flightdatabase.domain.InvalidField
import flightdatabase.domain.LongType
import flightdatabase.domain.ResultOrder
import flightdatabase.domain.StringType
import flightdatabase.domain.ValidatedSortAndLimit
import flightdatabase.domain.WrongOperator
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.partial.PartiallyAppliedGetAll
import flightdatabase.domain.partial.PartiallyAppliedGetBy
import flightdatabase.testutils._
import flightdatabase.testutils.implicits._
import org.http4s.Status._
import org.http4s.circe.CirceEntityCodec._
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction3
import org.scalamock.function.StubFunction5
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

final class AirlineAirplaneEndpointsTest
    extends IOEndpointsSpec
    with AnyFeatureSpecLike
    with GivenWhenThen
    with Matchers
    with CustomMatchers
    with MockFactory {

  val mockAlgebra: AirlineAirplaneAlgebra[IO] = stub[AirlineAirplaneAlgebra[IO]]
  override val api: Endpoints[IO] = AirlineAirplaneEndpoints[IO]("/airline-airplanes", mockAlgebra)

  val mockGetAll: PartiallyAppliedGetAll[IO, AirlineAirplane] =
    stub[PartiallyAppliedGetAll[IO, AirlineAirplane]]

  val mockGetBy: PartiallyAppliedGetBy[IO, AirlineAirplane] =
    stub[PartiallyAppliedGetBy[IO, AirlineAirplane]]

  val originalAirlineAirplanes: Nel[AirlineAirplane] = Nel.of(
    AirlineAirplane(1, 1, 2),
    AirlineAirplane(2, 1, 1),
    AirlineAirplane(3, 1, 3),
    AirlineAirplane(4, 2, 1),
    AirlineAirplane(5, 2, 3)
  )

  case class AirlineTest(name: String, iata: String)

  // airlineId -> AirlineTest(name, iata)
  val airlineIdMap: Map[Long, AirlineTest] = Map(
    1L -> AirlineTest("Lufthansa", "LH"),
    2L -> AirlineTest("Emirates", "EK")
  )

  case class AirplaneTest(name: String, capacity: Int)

  // airplaneId -> AirplaneTest(name, capacity)
  val airplaneIdMap: Map[Long, AirplaneTest] =
    Map(
      1L -> AirplaneTest("A380", 853),
      2L -> AirplaneTest("747-8", 410),
      3L -> AirplaneTest("A320neo", 194)
    )

  Feature("Checking if an airline-airplane exists") {
    Scenario("An airline-airplane exists") {
      Given("an existing airline-airplane ID")
      (mockAlgebra.doesAirlineAirplaneExist _).when(testId).returns(IO.pure(true))

      When("the airline-airplane is checked")
      val response = headResponse(createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the right method should be called only once")
      (mockAlgebra.doesAirlineAirplaneExist _).verify(testId).once()
    }

    Scenario("An airline-airplane does not exist") {
      Given("a non-existing airline-airplane ID")
      (mockAlgebra.doesAirlineAirplaneExist _).when(testId).returns(IO.pure(false))

      When("the airline-airplane is checked")
      val response = headResponse(createIdUri(testId))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the right method should be called only once")
      (mockAlgebra.doesAirlineAirplaneExist _).verify(testId).once()
    }

    Scenario("An airline-airplane ID is invalid") {
      Given("an invalid airline-airplane ID")

      When("the airline-airplane is checked")
      val response = headResponse(createIdUri(invalid))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("the right method should be called only once")
      (mockAlgebra.doesAirlineAirplaneExist _).verify(*).never()
    }
  }

  Feature("Fetching all airline-airplanes") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    val mockAirlineAirplanes
      : StubFunction1[ValidatedSortAndLimit, IO[ApiResult[Nel[AirlineAirplane]]]] =
      mockGetAll.apply(_: ValidatedSortAndLimit)

    def mockAirlineAirplanesOnly[V]
      : StubFunction3[ValidatedSortAndLimit, String, Read[V], IO[ApiResult[Nel[V]]]] =
      mockGetAll.apply(_: ValidatedSortAndLimit, _: String)(_: Read[V])

    Scenario("All airline-airplanes are fetched") {
      (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)

      Given("no query parameters")
      mockAirlineAirplanes
        .when(emptySortAndLimit)
        .returns(Got(originalAirlineAirplanes).elevate[IO])

      When("all airline-airplanes are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain all airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe originalAirlineAirplanes

      And("the right method should be called only once")
      mockAirlineAirplanes.verify(emptySortAndLimit).once()
      mockAirlineAirplanesOnly[Long].verify(*, *, *).never()
    }

    Scenario("Fetching only airline ID from all airline-airplanes") {
      val tableField = "airline_airplane.airline_id"
      val onlyAirlineIds = originalAirlineAirplanes.map(_.airlineId)
      (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)

      Given("query parameters to return only airline IDs")
      val query = "return-only=airline_id"
      mockAirlineAirplanesOnly[Long]
        .when(emptySortAndLimit, tableField, *)
        .returns(Got(onlyAirlineIds).elevate[IO])

      When("only airline IDs are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only airline IDs")
      response.extract[Nel[Long]] shouldBe onlyAirlineIds

      And("the right method should be called only once")
      mockAirlineAirplanesOnly[Long].verify(emptySortAndLimit, tableField, *).once()
      mockAirlineAirplanesOnly[String].verify(*, *, *).never()
      mockAirlineAirplanes.verify(*).never()
    }

    Scenario("Fetching and sorting only airplane ID for all airline-airplanes") {
      val tableField = "airline_airplane.airplane_id"
      val sortByAirplaneId = ValidatedSortAndLimit.sort(tableField)
      val sortedAirplaneIds = originalAirlineAirplanes.map(_.airplaneId).sorted
      (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)

      Given("query parameters to return and sort only airplane IDs")
      val query = "return-only=airplane_id&sort-by=airplane_id"
      mockAirlineAirplanesOnly[Long]
        .when(sortByAirplaneId, tableField, *)
        .returns(Got(sortedAirplaneIds).elevate[IO])

      When("only airplane IDs are fetched and sorted")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only sorted airplane IDs")
      response.extract[Nel[Long]] shouldBe sortedAirplaneIds

      And("the right method should be called only once")
      mockAirlineAirplanesOnly[Long].verify(sortByAirplaneId, tableField, *).once()
      mockAirlineAirplanesOnly[String].verify(*, *, *).never()
      mockAirlineAirplanes.verify(*).never()
    }

    Scenario("Fetching, sorting, and limiting only airline IDs for all airline-airplanes") {
      val readField = "airline_airplane.airline_id"
      val sortAndLimit = ValidatedSortAndLimit(
        sortBy = Some("airline_airplane.airplane_id"),
        order = Some(ResultOrder.Descending),
        limit = Some(1),
        offset = Some(1)
      )
      val sortedAirlineIds =
        Nel.one(originalAirlineAirplanes.sortBy(_.airplaneId).reverse.map(_.airlineId).tail.head)
      (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)

      Given("query parameters to return, sort, and limit only airline IDs")
      val query = "return-only=airline_id&sort-by=airplane_id&order=desc&limit=1&offset=1"
      mockAirlineAirplanesOnly[Long]
        .when(sortAndLimit, readField, *)
        .returns(Got(sortedAirlineIds).elevate[IO])

      When("only airline IDs are fetched, sorted, and limited")
      val response = getResponse(createQueryUri(query))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only sorted and limited airline IDs")
      response.extract[Nel[Long]] shouldBe sortedAirlineIds

      And("the right method should be called only once")
      mockAirlineAirplanesOnly[Long].verify(sortAndLimit, readField, *).once()
      mockAirlineAirplanesOnly[String].verify(*, *, *).never()
      mockAirlineAirplanes.verify(*).never()
    }

    Scenario("An empty list is returned") {
      (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)

      Given("no airline-airplanes in the database")
      mockAirlineAirplanes
        .when(emptySortAndLimit)
        .returns(EntryListEmpty.elevate[IO, Nel[AirlineAirplane]])

      When("all airline-airplanes are fetched")
      val response = getResponse()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should indicate an empty list")
      response.string shouldBe EntryListEmpty.error

      And("the right method should be called only once")
      mockAirlineAirplanes.verify(emptySortAndLimit).once()
      mockAirlineAirplanesOnly[Long].verify(*, *, *).never()
    }

    Scenario("An invalid return-only field is passed") {
      Given("an invalid return-only field")
      val query = s"return-only=$invalid"

      When("all airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe InvalidField(invalid).error

      And("no algebra methods should be called")
      mockAirlineAirplanes.verify(*).never()
      mockAirlineAirplanesOnly[Long].verify(*, *, *).never()
    }

    Scenario("Invalid sorting or limiting parameters are passed") {
      Given("invalid sorting or limiting parameters")
      val query = s"sort-by=$invalid&offset=-1"

      When("all airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string should includeAllOf(
        query.split("[&=]").toIndexedSeq: _*
      )

      And("no algebra methods should be called")
      mockAirlineAirplanes.verify(*).never()
      mockAirlineAirplanesOnly[Long].verify(*, *, *).never()
    }
  }

  Feature("Fetching an airline-airplane by IDs") {
    Scenario("Fetching an existing airline-airplane by primary ID") {
      Given("an existing airline-airplane ID")
      (mockAlgebra
        .getAirlineAirplane(_: Long))
        .when(testId)
        .returns(Got(originalAirlineAirplanes.head).elevate[IO])

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(testId))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplane")
      response.extract[AirlineAirplane] shouldBe originalAirlineAirplanes.head

      And("the right method should be called only once")
      (mockAlgebra.getAirlineAirplane(_: Long)).verify(testId).once()
    }

    Scenario("Fetching an existing airline-airplane by airline and airplane IDs") {
      Given("an existing airline and airplane ID")
      val airlineAirplane = originalAirlineAirplanes.head
      (mockAlgebra
        .getAirlineAirplane(_: Long, _: Long))
        .when(airlineAirplane.airlineId, airlineAirplane.airplaneId)
        .returns(Got(airlineAirplane).elevate[IO])

      When("the airline-airplane is fetched")
      val response = getResponse(
        createIdUri(s"airline/${airlineAirplane.airlineId}/airplane/${airlineAirplane.airplaneId}")
      )

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplane")
      response.extract[AirlineAirplane] shouldBe airlineAirplane

      And("the right method should be called only once")
      (mockAlgebra
        .getAirlineAirplane(_: Long, _: Long))
        .verify(airlineAirplane.airlineId, airlineAirplane.airplaneId)
        .once()
    }

    Scenario("Fetching a non-existing airline-airplane by primary ID") {
      val entryNotFound = EntryNotFound(testId)

      Given("a non-existing airline-airplane ID")
      (mockAlgebra
        .getAirlineAirplane(_: Long))
        .when(testId)
        .returns(entryNotFound.elevate[IO, AirlineAirplane])

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(testId))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the response body should contain the right error message")
      response.string shouldBe entryNotFound.error

      And("the right method should be called only once")
      (mockAlgebra.getAirlineAirplane(_: Long)).verify(testId).once()
    }

    Scenario("Fetching a non-existing airline-airplane by airline and airplane IDs") {
      val entryNotFound = EntryNotFound((testId, testId))

      Given("a non-existing airline and airplane ID")
      (mockAlgebra
        .getAirlineAirplane(_: Long, _: Long))
        .when(testId, testId)
        .returns(entryNotFound.elevate[IO, AirlineAirplane])

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(s"airline/$testId/airplane/$testId"))

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the response body should contain the right error message")
      response.string shouldBe entryNotFound.error

      And("the right method should be called only once")
      (mockAlgebra.getAirlineAirplane(_: Long, _: Long)).verify(testId, testId).once()
    }

    Scenario("Fetching an airline-airplane with invalid primary ID") {
      Given("an invalid airline-airplane ID")

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(invalid))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.getAirlineAirplane(_: Long)).verify(*).never()
      (mockAlgebra.getAirlineAirplane(_: Long, _: Long)).verify(*, *).never()
    }

    Scenario("Fetching an airline-airplane with an invalid airline ID") {
      Given("an invalid airline ID")

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(s"airline/$invalid/airplane/$testId"))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.getAirlineAirplane(_: Long)).verify(*).never()
      (mockAlgebra.getAirlineAirplane(_: Long, _: Long)).verify(*, *).never()
    }

    Scenario("Fetching an airline-airplane with an invalid airplane ID") {
      Given("an invalid airplane ID")

      When("the airline-airplane is fetched")
      val response = getResponse(createIdUri(s"airline/$testId/airplane/$invalid"))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      (mockAlgebra.getAirlineAirplane(_: Long)).verify(*).never()
      (mockAlgebra.getAirlineAirplane(_: Long, _: Long)).verify(*, *).never()
    }
  }

  Feature("Fetching airline-airplanes by an internal field") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    val path = Some("filter")
    def mockAirlineAirplanesBy[V]: StubFunction5[
      String,
      Nel[V],
      Operator,
      ValidatedSortAndLimit,
      Put[V],
      IO[ApiResult[Nel[AirlineAirplane]]]
    ] = mockGetBy.apply(_: String, _: Nel[V], _: Operator, _: ValidatedSortAndLimit)(_: Put[V])

    Scenario("Fetching airline-airplanes by airline ID") {
      val field = "airline_id"
      val airlineId = testId
      val selectedAirlineAirplanes =
        Nel.fromListUnsafe(originalAirlineAirplanes.filter(_.airlineId == airlineId))
      (() => mockAlgebra.getAirlineAirplanesBy).when().returns(mockGetBy)

      Given("an existing airline ID")
      mockAirlineAirplanesBy[Long]
        .when(field, Nel.of(airlineId), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(selectedAirlineAirplanes).elevate[IO])

      When("airline-airplanes are fetched by airline ID")
      val query = s"field=$field&value=$airlineId"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe selectedAirlineAirplanes

      And("the right method should be called only once")
      mockAirlineAirplanesBy[Long]
        .verify(field, Nel.of(airlineId), Operator.Equals, emptySortAndLimit, *)
        .once()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching airline-airplanes by a set of airplane IDs") {
      val field = "airplane_id"
      val airplaneIds = Nel.of(testId, testId + 1)
      val selectedAirlineAirplanes =
        Nel.fromListUnsafe(
          originalAirlineAirplanes.filter(aa => airplaneIds.exists(_ == aa.airplaneId))
        )
      (() => mockAlgebra.getAirlineAirplanesBy).when().returns(mockGetBy)

      Given("an existing airplane ID")
      mockAirlineAirplanesBy[Long]
        .when(field, airplaneIds, Operator.In, emptySortAndLimit, *)
        .returns(Got(selectedAirlineAirplanes).elevate[IO])

      When("airline-airplanes are fetched by airplane ID")
      val query = s"field=$field&operator=in&value=${airplaneIds.mkString_(",")}"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe selectedAirlineAirplanes

      And("the right method should be called only once")
      mockAirlineAirplanesBy[Long]
        .verify(field, airplaneIds, Operator.In, emptySortAndLimit, *)
        .once()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching by airplane ID and sorting by airline ID") {
      val field = "airplane_id"
      val filterOutAirplaneIds = Nel.of(testId, testId + 1)
      val sortedAirlineAirplanes =
        Nel.fromListUnsafe(
          originalAirlineAirplanes
            .filterNot(aa => filterOutAirplaneIds.exists(_ == aa.airplaneId))
            .sortBy(_.airlineId)
            .reverse
        )

      val sortAndLimit = ValidatedSortAndLimit.sortDescending("airline_airplane.airline_id")
      (() => mockAlgebra.getAirlineAirplanesBy).when().returns(mockGetBy)

      Given("a set of airplane IDs to filter out")
      mockAirlineAirplanesBy[Long]
        .when(field, filterOutAirplaneIds, Operator.NotIn, sortAndLimit, *)
        .returns(Got(sortedAirlineAirplanes).elevate[IO])

      When("airline-airplanes are fetched by airplane ID and sorted by airline ID")
      val query =
        s"field=$field&operator=not_in&value=${filterOutAirplaneIds.mkString_(",")}&sort-by=airline_id&order=desc"
      val response = getResponse(createQueryUri(query, path))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe sortedAirlineAirplanes

      And("the right method should be called only once")
      mockAirlineAirplanesBy[Long]
        .verify(field, filterOutAirplaneIds, Operator.NotIn, sortAndLimit, *)
        .once()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid field") {
      Given("an invalid field")
      val query = s"field=$invalid&value=1"

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe InvalidField(invalid).error

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Empty field") {
      Given("an empty field")
      val query = "field=&value=1"

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe InvalidField("").error

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator") {
      Given("an invalid operator")
      val query = s"field=airline_id&operator=$invalid&value=1"

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string should include(invalid)

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator for given field type") {
      val field = "airline_id"
      val invalidOperator = Operator.EndsWith

      Given("an invalid operator for the given field type")
      val query = s"field=$field&operator=${invalidOperator.entryName}&value=1"

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe WrongOperator(invalidOperator, field, LongType).error

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid filter path") {
      Given("an invalid filter path")
      val invalidPath = Some(invalid)

      When("airline-airplanes are fetched")
      val query = "field=id&value=1"
      val response = getResponse(createQueryUri(query, invalidPath))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }

    Scenario("No query parameters passed") {
      Given("no query parameters")
      val query = ""

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlineAirplanesBy[Long].verify(*, *, *, *, *).never()
      mockAirlineAirplanesBy[String].verify(*, *, *, *, *).never()
    }
  }

  Feature("Fetching airline-airplanes by an external field") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    def path(table: String): Option[String] = Some(s"$table/filter")
    def mockAirlinesByExternal[V]: StubFunction5[
      String,
      Nel[V],
      Operator,
      ValidatedSortAndLimit,
      Put[V],
      IO[ApiResult[Nel[AirlineAirplane]]]
    ] =
      mockGetBy.apply(_: String, _: Nel[V], _: Operator, _: ValidatedSortAndLimit)(_: Put[V])

    Scenario("Fetching airline-airplanes by airline name") {
      val field = "name"
      val airline = "Lufthansa"
      val airlineAirplanesByAirline = Nel.fromListUnsafe(
        originalAirlineAirplanes.filter(aa => airlineIdMap(aa.airlineId).name == airline)
      )
      (() => mockAlgebra.getAirlineAirplanesByAirline).when().returns(mockGetBy)

      Given("an airline name")
      mockAirlinesByExternal[String]
        .when(field, Nel.of(airline), Operator.Equals, emptySortAndLimit, *)
        .returns(Got(airlineAirplanesByAirline).elevate[IO])

      When("airline-airplanes are fetched by airline name")
      val query = s"field=$field&value=$airline"
      val response = getResponse(createQueryUri(query, path("airline")))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe airlineAirplanesByAirline

      And("the right method should be called only once")
      mockAirlinesByExternal[String]
        .verify(field, Nel.of(airline), Operator.Equals, emptySortAndLimit, *)
        .once()
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
    }

    Scenario("Fetching airline-airplanes by airplane capacity and sorting by airplane name") {
      val field = "capacity"
      val minCapacity = 400
      val airlineAirplanesByCapacity = Nel
        .fromListUnsafe(
          originalAirlineAirplanes.filter(aa =>
            airplaneIdMap(aa.airplaneId).capacity >= minCapacity
          )
        )
        .sortBy(aa => airplaneIdMap(aa.airplaneId).name)
        .reverse
      val sortAndLimit = ValidatedSortAndLimit.sortDescending("airplane.name")
      (() => mockAlgebra.getAirlineAirplanesByAirplane).when().returns(mockGetBy)

      Given("a minimum airplane capacity")
      mockAirlinesByExternal[Int]
        .when(field, Nel.of(minCapacity), Operator.GreaterThanOrEqualTo, sortAndLimit, *)
        .returns(Got(airlineAirplanesByCapacity).elevate[IO])

      When("airline-airplanes are fetched by airplane capacity and sorted by airplane name")
      val query = s"field=$field&operator=gteq&value=$minCapacity&sort-by=name&order=desc"
      val response = getResponse(createQueryUri(query, path("airplane")))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain the right airline-airplanes")
      response.extract[Nel[AirlineAirplane]] shouldBe airlineAirplanesByCapacity

      And("the right method should be called only once")
      mockAirlinesByExternal[Int]
        .verify(field, Nel.of(minCapacity), Operator.GreaterThanOrEqualTo, sortAndLimit, *)
        .once()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid field") {
      Given("an invalid airline field")
      val query = s"field=$invalid&value=1"

      When("airline-airplanes are fetched by an airline field")
      val response = getResponse(createQueryUri(query, path("airline")))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe InvalidField(invalid).error

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("Empty field") {
      Given("an empty airplane field")
      val query = "field=&value=1"

      When("airline-airplanes are fetched by an airplane field")
      val response = getResponse(createQueryUri(query, path("airplane")))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe InvalidField("").error

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator") {
      Given("an invalid operator")
      val query = s"field=name&operator=$invalid&value=1"

      When("airline-airplanes are fetched by an airline field")
      val response = getResponse(createQueryUri(query, path("airline")))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string should include(invalid)

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid operator because of field type") {
      val field = "name"
      val invalidOperator = Operator.GreaterThan

      Given("an invalid operator for the given field type")
      val query = s"field=$field&operator=${invalidOperator.entryName}&value=1"

      When("airline-airplanes are fetched by an airline field")
      val response = getResponse(createQueryUri(query, path("airline")))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe WrongOperator(invalidOperator, field, StringType).error

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("Invalid filter path") {
      Given("an invalid filter path")
      val invalidPath = Some(s"airline/$invalid")

      When("airline-airplanes are fetched")
      val query = "field=id&value=1"
      val response = getResponse(createQueryUri(query, invalidPath))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }

    Scenario("No query parameters passed") {
      Given("no query parameters")
      val query = ""

      When("airline-airplanes are fetched")
      val response = getResponse(createQueryUri(query, path("airline")))

      Then("a 400 status is returned")
      response.status shouldBe BadRequest

      And("the response body should contain the right error message")
      response.string shouldBe EntryInvalidFormat.error

      And("no algebra methods should be called")
      mockAirlinesByExternal[Long].verify(*, *, *, *, *).never()
      mockAirlinesByExternal[String].verify(*, *, *, *, *).never()
    }
  }
}
