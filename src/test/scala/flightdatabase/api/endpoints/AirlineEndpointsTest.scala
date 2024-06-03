package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Read
import flightdatabase.domain.{ApiResult, ResultOrder, ValidatedSortAndLimit}
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.testutils._
import flightdatabase.testutils.implicits._
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status._
import org.http4s.circe.CirceEntityCodec._
import org.scalamock.function.StubFunction3
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

final class AirlineEndpointsTest
    extends AnyFeatureSpec
    with GivenWhenThen
    with Matchers
    with MockFactory {
  val mockAlgebra: AirlineAlgebra[IO] = stub[AirlineAlgebra[IO]]
  val endpoints: HttpApp[IO] = AirlineEndpoints[IO]("/airlines", mockAlgebra).endpoints.orNotFound

  val originalAirlines: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  Feature("Checking if an airline exists") {
    Scenario("An airline exists") {
      Given("an existing airline ID")
      (mockAlgebra.doesAirlineExist _).when(testId).returns(IO.pure(true))

      When("the airline is checked")
      val response = endpoints
        .run(Request(method = Method.HEAD, uri = createIdUri(testId)))
        .unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the method should be called only once")
      (mockAlgebra.doesAirlineExist _).verify(testId).once()
    }

    Scenario("An airline does not exist") {
      Given("a non-existing airline ID")
      (mockAlgebra.doesAirlineExist _).when(testId).returns(IO.pure(false))

      When("the airline is checked")
      val response = endpoints
        .run(Request(method = Method.HEAD, uri = createIdUri(testId)))
        .unsafeRunSync()

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the method should be called only once")
      (mockAlgebra.doesAirlineExist _).verify(testId).once()
    }

    Scenario("An invalid airline ID") {
      Given("an invalid airline ID")

      When("the airline is checked")
      val response = endpoints
        .run(Request(method = Method.HEAD, uri = createIdUri(invalidTestId)))
        .unsafeRunSync()

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the method should not be called")
      (mockAlgebra.doesAirlineExist _).verify(*).never()
    }
  }

  Feature("Fetching airlines") {
    val emptySortAndLimit = ValidatedSortAndLimit.empty
    def mockAirlinesOnly[V]
      : StubFunction3[ValidatedSortAndLimit, String, Read[V], IO[ApiResult[Nel[V]]]] =
      mockAlgebra.getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[V])

    Scenario("Fetching all airlines") {
      Given("no query parameters")
      (mockAlgebra.getAirlines _).when(emptySortAndLimit).returns(originalAirlines.asResult[IO])

      When("all airlines are fetched")
      val response = endpoints.run(Request(method = Method.GET)).unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain all airlines")
      response.as[Nel[Airline]].unsafeRunSync() shouldBe originalAirlines

      And("the right method should be called only once")
      (mockAlgebra.getAirlines _).verify(emptySortAndLimit).once()
      mockAirlinesOnly[String].verify(*, *, *).never()
    }

    Scenario("Fetching only name field for all airlines") {
      val tableField = "airline.name"
      val onlyAirlineNames = originalAirlines.map(_.name)

      Given("query parameters to return only the name field")
      val query = "return-only=name"
      mockAirlinesOnly[String]
        .when(emptySortAndLimit, tableField, *)
        .returns(onlyAirlineNames.asResult[IO])

      When("all airlines are fetched")
      val response = endpoints
        .run(Request(method = Method.GET, uri = createQueryUri(query, None)))
        .unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field")
      response.as[Nel[String]].unsafeRunSync() shouldBe onlyAirlineNames

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(emptySortAndLimit, tableField, *).once()
      mockAirlinesOnly[Long].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }

    Scenario("Fetching and sorting only name fields for all airlines") {
      val tableField = "airline.name"
      val sortByName = ValidatedSortAndLimit.sort(tableField)
      val airlineNamesSorted = originalAirlines.map(_.name).sorted

      Given("query parameters to return only the name field and sort by it")
      val query = "return-only=name&sort-by=name"
      mockAirlinesOnly[String]
        .when(sortByName, tableField, *)
        .returns(airlineNamesSorted.asResult[IO])

      When("all airlines are fetched")
      val response = endpoints
        .run(Request(method = Method.GET, uri = createQueryUri(query, None)))
        .unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field sorted")
      response.as[Nel[String]].unsafeRunSync() shouldBe airlineNamesSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortByName, tableField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }

    Scenario("Fetching, sorting, and limiting only IATA fields for all airlines") {
      val readField = "airline.iata"
      val sortAndLimit = ValidatedSortAndLimit(
        sortBy = Some("airline.name"),
        order = Some(ResultOrder.Descending),
        limit = Some(1),
        offset = Some(1)
      )
      val airlineIataSorted = Nel.one(originalAirlines.map(_.iata).sorted.reverse.tail.head)

      Given("query parameters to return only IATA, sort by name in reverse, and take second result")
      val query = s"return-only=iata&sort-by=name&order=desc&limit=1&offset=1"
      mockAirlinesOnly[String]
        .when(sortAndLimit, readField, *)
        .returns(airlineIataSorted.asResult[IO])

      When("all airlines are fetched")
      val response = endpoints
        .run(Request(method = Method.GET, uri = createQueryUri(query, None)))
        .unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the IATA field sorted and limited")
      response.as[Nel[String]].unsafeRunSync() shouldBe airlineIataSorted

      And("the right method should be called only once")
      mockAirlinesOnly[String].verify(sortAndLimit, readField, *).once()
      mockAirlinesOnly[BigDecimal].verify(*, *, *).never()
      (mockAlgebra.getAirlines _).verify(*).never()
    }
  }
}
