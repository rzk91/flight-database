package flightdatabase.api.testutils.endpoints

import cats.effect.IO
import flightdatabase._
import flightdatabase.api.testutils._
import flightdatabase.test.syntax.all._
import org.http4s.Status._
import org.http4s.circe.CirceEntityCodec._
import org.scalamock.function.StubFunction2

/**
  * Opt-in behavior for junction entities exposing a two-id lookup, e.g.
  * `GET /airline-cities/airline/{airlineId}/city/{cityId}`.
  *
  * Mix into an [[EntityEndpointsSpec]] and call `testCompositeIdBehavior()` from the spec body
  * (after the vals are initialised, like the other behaviors). The entity supplies the two-arg
  * stub and the path builder; everything else is shared.
  *
  * Note on invalid IDs: whether the route binds segments via `LongVar` (non-numeric falls through
  * to `orBadRequest`) or via a string segment decoded with `.asLong` (matches, then yields
  * `EntryInvalidFormat`), both produce a 400 with the `EntryInvalidFormat` body, so the invalid-ID
  * scenarios below hold for either style.
  */
trait CompositeIdBehavior[Model, Create, Patch] {
  self: EntityEndpointsSpec[Model, Create, Patch] =>

  /** The two-argument lookup, e.g. `getAirlineCity(_: Long, _: Long)`. */
  def compositeStub: StubFunction2[Long, Long, IO[ApiResult[Model]]]

  /** Builds the composite path WITHOUT a leading slash, e.g. `s"airline/$leftId/city/$rightId"`. */
  def compositePath(leftId: String, rightId: String): String

  protected def testCompositeIdBehavior(): Unit =
    Feature(s"Fetching a ${table.asString} by a composite ID") {
      Scenario("Fetching by an existing composite ID") {
        Given("an existing pair of IDs")
        compositeStub.when(testId, testId).returns(Got(samples.head).elevate[IO])

        When("the entity is fetched")
        val response = getResponse(createIdUri(compositePath(testId.toString, testId.toString)))

        Then("a 200 status is returned")
        response.status shouldBe Ok

        And("the body contains the entity")
        response.extract[Model] shouldBe samples.head

        And("the method is called once")
        compositeStub.verify(testId, testId).once()
      }

      Scenario("Fetching by a non-existing composite ID") {
        val notFound = EntryNotFound((testId, testId))
        Given("a non-existing pair of IDs")
        compositeStub.when(testId, testId).returns(notFound.elevate[IO, Model])

        When("the entity is fetched")
        val response = getResponse(createIdUri(compositePath(testId.toString, testId.toString)))

        Then("a 404 status is returned")
        response.status shouldBe NotFound

        And("the body contains the error")
        response.string shouldBe notFound.error

        And("the method is called once")
        compositeStub.verify(testId, testId).once()
      }

      Scenario("Fetching with an invalid left ID") {
        Given("an invalid left ID")

        When("the entity is fetched")
        val response = getResponse(createIdUri(compositePath(invalid, testId.toString)))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the right error message is returned")
        response.string shouldBe EntryInvalidFormat.error

        And("the method is never called")
        compositeStub.verify(*, *).never()
      }

      Scenario("Fetching with an invalid right ID") {
        Given("an invalid right ID")

        When("the entity is fetched")
        val response = getResponse(createIdUri(compositePath(testId.toString, invalid)))

        Then("a 400 status is returned")
        response.status shouldBe BadRequest

        And("the right error message is returned")
        response.string shouldBe EntryInvalidFormat.error

        And("the method is never called")
        compositeStub.verify(*, *).never()
      }
    }
}
