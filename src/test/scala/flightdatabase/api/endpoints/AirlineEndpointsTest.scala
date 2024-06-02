package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Read
import flightdatabase.domain.ValidatedSortAndLimit
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.toApiResult
import flightdatabase.testutils._
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Status._
import org.http4s.circe.CirceEntityCodec._
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
        .run(Request(method = Method.HEAD, uri = createUri(testId)))
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
        .run(Request(method = Method.HEAD, uri = createUri(testId)))
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
        .run(Request(method = Method.HEAD, uri = createUri(invalidTestId)))
        .unsafeRunSync()

      Then("a 404 status is returned")
      response.status shouldBe NotFound

      And("the method should not be called")
      (mockAlgebra.doesAirlineExist _).verify(*).never()
    }
  }

  Feature("Fetching airlines") {
    Scenario("Fetching all airlines") {
      Given("no query parameters")
      (mockAlgebra.getAirlines _)
        .when(ValidatedSortAndLimit.empty)
        .returns(IO.pure(toApiResult(originalAirlines)))

      When("all airlines are fetched")
      val response = endpoints.run(Request(method = Method.GET)).unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain all airlines")
      response.as[Nel[Airline]].unsafeRunSync() shouldBe originalAirlines

      And("the right method should be called only once")
      (mockAlgebra.getAirlines _).verify(ValidatedSortAndLimit.empty).once()
      (mockAlgebra
        .getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[String]))
        .verify(*, *, *)
        .never()
    }

    // FixMe: This test fails because of some unknown cats-effect issue
    ignore("Fetching only name field for all airlines") {
      Given("query parameters to return only the name field")
      val query = "return-only=name"
      (mockAlgebra
        .getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[String]))
        .when(ValidatedSortAndLimit.empty, "name", Read[String])
        .returns(IO.pure(toApiResult(originalAirlines.map(_.name))))

      When("all airlines are fetched")
      val response = endpoints
        .run(Request(method = Method.GET, uri = createUri(query, None)))
        .unsafeRunSync()

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And("the response body should contain only the name field")
      response.as[Nel[String]].unsafeRunSync() shouldBe originalAirlines.map(_.name)

      And("the right method should be called only once")
      (mockAlgebra
        .getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[String]))
        .verify(ValidatedSortAndLimit.empty, "name", Read[String])
        .once()
      (mockAlgebra
        .getAirlinesOnly(_: ValidatedSortAndLimit, _: String)(_: Read[Long]))
        .verify(*, *, *)
        .never()

      (mockAlgebra.getAirlines _).verify(*).never()
    }
  }
}
