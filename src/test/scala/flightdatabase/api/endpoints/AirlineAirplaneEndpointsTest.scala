package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import doobie.Read
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryInvalidFormat
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.Got
import flightdatabase.domain.InvalidField
import flightdatabase.domain.ResultOrder
import flightdatabase.domain.ValidatedSortAndLimit
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
  val airlineIdMap: Map[Long, (String, String)] = Map(
    1L -> ("Lufthansa", "LH"),
    2L -> ("Emirates", "EK")
  )

  case class AirplaneTest(name: String, capacity: Int)

  // airplaneId -> AirplaneTest(name, capacity)
  val airplaneIdMap: Map[Long, (String, Int)] =
    Map(1L -> ("A380", 853), 2L -> ("747-8", 410), 3L -> ("A320neo", 194))

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
}
