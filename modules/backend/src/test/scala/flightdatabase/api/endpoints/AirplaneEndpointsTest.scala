package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.airplane.Airplane
import flightdatabase.airplane.AirplaneAlgebra
import flightdatabase.airplane.AirplaneCreate
import flightdatabase.airplane.AirplanePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class AirplaneEndpointsTest
    extends EntityEndpointsSpec[Airplane, AirplaneCreate, AirplanePatch] {

  val mockAlgebra: AirplaneAlgebra[IO] = stub[AirplaneAlgebra[IO]]
  override val api: Endpoints[IO] = AirplaneEndpoints[IO]("/airplanes", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, Airplane]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, Airplane]]

  val table: TableBase[Airplane] = Airplane.airplaneTableBase
  val modelDecoder: Decoder[Airplane] = Decoder[Airplane]
  val modelEncoder: Encoder[Airplane] = Encoder[Airplane]
  val createEncoder: Encoder[AirplaneCreate] = Encoder[AirplaneCreate]
  val patchEncoder: Encoder[AirplanePatch] = Encoder[AirplanePatch]

  // Mirrors `originalAirplanes` in AirplaneRepositoryIT.
  val samples: Nel[Airplane] = Nel.of(
    Airplane(1, "A380", 1, 853, 14800),
    Airplane(2, "747-8", 2, 410, 14310),
    Airplane(3, "A320neo", 1, 194, 6300),
    Airplane(4, "787-8", 2, 248, 13530)
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "A380", Operator.Equals, StringType),
    FieldFixture("capacity", 200, Operator.GreaterThan, IntType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  // Manufacturer is the external table reachable via /airplanes/manufacturer/filter.
  private val manufacturerFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Airbus", Operator.Equals, StringType),
    FieldFixture("base_city_id", 5L, Operator.In, LongType)
  )

  def existsStub = mockAlgebra.doesAirplaneExist _
  def getByIdStub = mockAlgebra.getAirplane _
  def createStub = mockAlgebra.createAirplane _
  def updateStub = mockAlgebra.updateAirplane _
  def patchStub = mockAlgebra.partiallyUpdateAirplane _
  def removeStub = mockAlgebra.removeAirplane _

  def armGetAll(): Unit = (() => mockAlgebra.getAirplanes).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirplanesBy).when().returns(mockGetBy)

  private def armGetByManufacturer(): Unit =
    (() => mockAlgebra.getAirplanesByManufacturer).when().returns(mockGetBy)

  val sampleCreate: AirplaneCreate = AirplaneCreate("A350", 1, 325, 13900)
  def fromCreate(id: Long, create: AirplaneCreate): Airplane = Airplane.fromCreate(id, create)
  def withCreateId(create: AirplaneCreate, id: Long): AirplaneCreate = create.copy(id = Some(id))
  val samplePatch: AirplanePatch = AirplanePatch(name = Some("A350_patched"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("manufacturer", () => armGetByManufacturer(), manufacturerFixtures)
  testCrudBehavior()
}
