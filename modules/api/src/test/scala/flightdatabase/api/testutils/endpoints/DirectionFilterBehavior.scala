package flightdatabase.api.testutils.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import doobie.Put
import flightdatabase._
import flightdatabase.api.testutils._
import flightdatabase.partial.PartiallyAppliedGetBy
import org.http4s.Status._
import org.scalamock.function.StubFunction1

/**
  * Opt-in behavior for an external `/<segment>/filter` whose accessor is parameterised by an
  * `inbound`/`outbound` flag pair, e.g. AirlineRoute's
  * `GET /airline-routes/airport/filter?...&inbound&outbound` calling
  * `getAirlineRoutesByAirport(direction: Option[Boolean])`.
  *
  * It verifies the flag -> direction mapping (`inbound` -> Some(true), `outbound` -> Some(false),
  * both / neither -> None), which is the unique concern beyond the standard external filter. Run
  * it ALONGSIDE `testExternalFilterBehavior(segment, ...)` (which covers the field/error scenarios
  * with the default `None` direction).
  */
trait DirectionFilterBehavior[Model, Create, Patch] {
  self: EntityEndpointsSpec[Model, Create, Patch] =>

  /** The flag-parameterised accessor, e.g. `mockAlgebra.getAirlineRoutesByAirport _`. */
  def directionStub: StubFunction1[Option[Boolean], PartiallyAppliedGetBy[IO, Model]]

  protected def testDirectionFilterBehavior[V](segment: String, fixture: FieldFixture[V]): Unit =
    Feature(s"Fetching ${table.asString}s by a $segment field with a direction flag") {
      directionScenario(segment, "inbound", Some(true), fixture)
      directionScenario(segment, "outbound", Some(false), fixture)
      directionScenario(segment, "inbound&outbound", None, fixture)
      directionScenario(segment, "", None, fixture)
    }

  private def directionScenario[V](
    segment: String,
    flagQuery: String,
    expected: Option[Boolean],
    fixture: FieldFixture[V]
  ): Unit = {
    val label = if (flagQuery.isEmpty) "no flag" else flagQuery
    Scenario(s"Filtering with '$label' maps to direction $expected") {
      implicit val put: Put[V] = fixture.put
      Given(s"a $segment filter with '$label'")
      directionStub.when(expected).returns(mockGetBy)
      mockBy[V]
        .when(fixture.field, Nel.one(fixture.value), fixture.operator, emptySortAndLimit, *)
        .returns(Got(samples).elevate[IO])

      When("entities are fetched")
      val base =
        s"field=${fixture.field}&value=${fixture.valueString}&operator=${fixture.operator.entryName}"
      val query = if (flagQuery.isEmpty) base else s"$base&$flagQuery"
      val response = getResponse(createQueryUri(query, Some(s"$segment/filter")))

      Then("a 200 status is returned")
      response.status shouldBe Ok

      And(s"the accessor is called once with direction $expected")
      directionStub.verify(expected).once()
    }
  }
}
