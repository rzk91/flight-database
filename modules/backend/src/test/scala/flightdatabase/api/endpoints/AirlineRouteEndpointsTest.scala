package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.airline_route.AirlineRoute
import flightdatabase.airline_route.AirlineRouteAlgebra
import flightdatabase.airline_route.AirlineRouteCreate
import flightdatabase.airline_route.AirlineRoutePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.DirectionFilterBehavior
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class AirlineRouteEndpointsTest
    extends EntityEndpointsSpec[AirlineRoute, AirlineRouteCreate, AirlineRoutePatch]
    with DirectionFilterBehavior[AirlineRoute, AirlineRouteCreate, AirlineRoutePatch] {

  val mockAlgebra: AirlineRouteAlgebra[IO] = stub[AirlineRouteAlgebra[IO]]
  override val api: Endpoints[IO] = AirlineRouteEndpoints[IO]("/airline-routes", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, AirlineRoute]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, AirlineRoute]]

  val table: TableBase[AirlineRoute] = AirlineRoute.airlineRouteTableBase
  val modelDecoder: Decoder[AirlineRoute] = Decoder[AirlineRoute]
  val modelEncoder: Encoder[AirlineRoute] = Encoder[AirlineRoute]
  val createEncoder: Encoder[AirlineRouteCreate] = Encoder[AirlineRouteCreate]
  val patchEncoder: Encoder[AirlineRoutePatch] = Encoder[AirlineRoutePatch]

  // Mirrors `originalAirlineRoutes` in AirlineRouteRepositoryIT.
  val samples: Nel[AirlineRoute] = Nel.of(
    AirlineRoute(1, 1, "LH754", 1, 2),
    AirlineRoute(2, 1, "LH755", 2, 1),
    AirlineRoute(3, 5, "EK565", 2, 3),
    AirlineRoute(4, 5, "EK566", 3, 2),
    AirlineRoute(5, 4, "EK47", 3, 1),
    AirlineRoute(6, 4, "EK46", 1, 3)
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("route_number", "LH754", Operator.Equals, StringType),
    FieldFixture("airline_airplane_id", 1L, Operator.In, LongType)
  )

  private val airlineFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Lufthansa", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  private val airplaneFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "A380", Operator.Equals, StringType),
    FieldFixture("capacity", 400, Operator.GreaterThan, IntType)
  )

  private val airportFixtures: List[FieldFixture[_]] = List(
    FieldFixture("icao", "EDDF", Operator.Equals, StringType),
    FieldFixture("number_of_runways", 1, Operator.GreaterThan, IntType)
  )

  def existsStub = mockAlgebra.doesAirlineRouteExist _
  def getByIdStub = mockAlgebra.getAirlineRoute _
  def createStub = mockAlgebra.createAirlineRoute _
  def updateStub = mockAlgebra.updateAirlineRoute _
  def patchStub = mockAlgebra.partiallyUpdateAirlineRoute _
  def removeStub = mockAlgebra.removeAirlineRoute _

  def armGetAll(): Unit = (() => mockAlgebra.getAirlineRoutes).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirlineRoutesBy).when().returns(mockGetBy)

  private def armByAirline(): Unit =
    (() => mockAlgebra.getAirlineRoutesByAirline).when().returns(mockGetBy)

  private def armByAirplane(): Unit =
    (() => mockAlgebra.getAirlineRoutesByAirplane).when().returns(mockGetBy)

  // The airport accessor is parameterised by the inbound/outbound direction flag.
  def directionStub = mockAlgebra.getAirlineRoutesByAirport _

  val sampleCreate: AirlineRouteCreate = AirlineRouteCreate(4, "EK48", 3, 1)

  def fromCreate(id: Long, create: AirlineRouteCreate): AirlineRoute =
    AirlineRoute.fromCreate(id, create)

  def withCreateId(create: AirlineRouteCreate, id: Long): AirlineRouteCreate =
    create.copy(id = Some(id))
  val samplePatch: AirlineRoutePatch = AirlineRoutePatch(route = Some("EK50"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("airline", () => armByAirline(), airlineFixtures)
  testExternalFilterBehavior("airplane", () => armByAirplane(), airplaneFixtures)
  testExternalFilterBehavior(
    "airport",
    () => directionStub.when(None).returns(mockGetBy),
    airportFixtures
  )
  testDirectionFilterBehavior("airport", airportFixtures.head)
  testCrudBehavior()
}
