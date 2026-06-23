package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.airline_airplane.AirlineAirplane
import flightdatabase.airline_airplane.AirlineAirplaneAlgebra
import flightdatabase.airline_airplane.AirlineAirplaneCreate
import flightdatabase.airline_airplane.AirlineAirplanePatch
import flightdatabase.api.testutils.endpoints._
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class AirlineAirplaneEndpointsTest
    extends EntityEndpointsSpec[AirlineAirplane, AirlineAirplaneCreate, AirlineAirplanePatch]
    with CompositeIdBehavior[AirlineAirplane, AirlineAirplaneCreate, AirlineAirplanePatch] {

  val mockAlgebra: AirlineAirplaneAlgebra[IO] = stub[AirlineAirplaneAlgebra[IO]]

  override val api: Endpoints[IO] =
    AirlineAirplaneEndpoints[IO]("/airline-airplanes", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, AirlineAirplane] =
    stub[PartiallyAppliedGetAll[IO, AirlineAirplane]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, AirlineAirplane] =
    stub[PartiallyAppliedGetBy[IO, AirlineAirplane]]

  val table: TableBase[AirlineAirplane] = AirlineAirplane.airlineAirplaneTableBase
  val modelDecoder: Decoder[AirlineAirplane] = Decoder[AirlineAirplane]
  val modelEncoder: Encoder[AirlineAirplane] = Encoder[AirlineAirplane]
  val createEncoder: Encoder[AirlineAirplaneCreate] = Encoder[AirlineAirplaneCreate]
  val patchEncoder: Encoder[AirlineAirplanePatch] = Encoder[AirlineAirplanePatch]

  // Mirrors `originalAirlineAirplanes` in AirlineAirplaneRepositoryIT.
  val samples: Nel[AirlineAirplane] = Nel.of(
    AirlineAirplane(1, 1, 2),
    AirlineAirplane(2, 1, 1),
    AirlineAirplane(3, 1, 3),
    AirlineAirplane(4, 2, 1),
    AirlineAirplane(5, 2, 3)
  )

  // Own fields are all Long.
  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("airline_id", 1L, Operator.Equals, LongType)
  )

  private val airlineFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Lufthansa", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  private val airplaneFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "A380", Operator.Equals, StringType),
    FieldFixture("capacity", 400, Operator.GreaterThan, IntType)
  )

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesAirlineAirplaneExist _

  def getByIdStub: StubFunction1[Long, IO[ApiResult[AirlineAirplane]]] =
    mockAlgebra.getAirlineAirplane(_: Long)

  def compositeStub: StubFunction2[Long, Long, IO[ApiResult[AirlineAirplane]]] =
    mockAlgebra.getAirlineAirplane(_: Long, _: Long)

  def compositePath(leftId: String, rightId: String): String =
    s"airline/$leftId/airplane/$rightId"

  def createStub: StubFunction1[AirlineAirplaneCreate, IO[ApiResult[Long]]] =
    mockAlgebra.createAirlineAirplane _

  def updateStub: StubFunction1[AirlineAirplane, IO[ApiResult[Long]]] =
    mockAlgebra.updateAirlineAirplane _

  def patchStub: StubFunction2[Long, AirlineAirplanePatch, IO[ApiResult[AirlineAirplane]]] =
    mockAlgebra.partiallyUpdateAirlineAirplane _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeAirlineAirplane _

  def armGetAll(): Unit = (() => mockAlgebra.getAirlineAirplanes).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirlineAirplanesBy).when().returns(mockGetBy)

  private def armByAirline(): Unit =
    (() => mockAlgebra.getAirlineAirplanesByAirline).when().returns(mockGetBy)

  private def armByAirplane(): Unit =
    (() => mockAlgebra.getAirlineAirplanesByAirplane).when().returns(mockGetBy)

  val sampleCreate: AirlineAirplaneCreate = AirlineAirplaneCreate(2, 2)

  def fromCreate(id: Long, create: AirlineAirplaneCreate): AirlineAirplane =
    AirlineAirplane.fromCreate(id, create)

  def withCreateId(create: AirlineAirplaneCreate, id: Long): AirlineAirplaneCreate =
    create.copy(id = Some(id))
  val samplePatch: AirlineAirplanePatch = AirlineAirplanePatch(airplaneId = Some(2))

  testExistenceBehavior()
  testGetByIdBehavior()
  testCompositeIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("airline", () => armByAirline(), airlineFixtures)
  testExternalFilterBehavior("airplane", () => armByAirplane(), airplaneFixtures)
  testCrudBehavior()
}
