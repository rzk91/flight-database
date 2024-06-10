package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.domain.partial.PartiallyAppliedGetAll
import flightdatabase.domain.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.CustomMatchers
import flightdatabase.testutils.IOEndpointsSpec
import flightdatabase.testutils.createIdUri
import flightdatabase.testutils.testId
import org.http4s.Status._
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
  }
}
