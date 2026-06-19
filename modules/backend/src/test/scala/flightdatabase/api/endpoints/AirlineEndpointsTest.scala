package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.airline.Airline
import flightdatabase.airline.AirlineAlgebra
import flightdatabase.airline.AirlineCreate
import flightdatabase.airline.AirlinePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class AirlineEndpointsTest extends EntityEndpointsSpec[Airline, AirlineCreate, AirlinePatch] {

  val mockAlgebra: AirlineAlgebra[IO] = stub[AirlineAlgebra[IO]]
  override val api: Endpoints[IO] = AirlineEndpoints[IO]("/airlines", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, Airline]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, Airline]]

  val table: TableBase[Airline] = Airline.airlineTableBase
  val modelDecoder: Decoder[Airline] = Decoder[Airline]
  val modelEncoder: Encoder[Airline] = Encoder[Airline]
  val createEncoder: Encoder[AirlineCreate] = Encoder[AirlineCreate]
  val patchEncoder: Encoder[AirlinePatch] = Encoder[AirlinePatch]

  // Mirrors `originalAirlines` in AirlineRepositoryIT.
  val samples: Nel[Airline] = Nel.of(
    Airline(1, "Lufthansa", "LH", "DLH", "LUFTHANSA", 2),
    Airline(2, "Emirates", "EK", "UAE", "EMIRATES", 4)
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Lufthansa", Operator.Equals, StringType),
    FieldFixture("icao", "DLH", Operator.NotIn, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  // Country is the external table reachable via /airlines/country/filter.
  private val countryFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Germany", Operator.Equals, StringType),
    FieldFixture("country_code", 49, Operator.GreaterThan, IntType)
  )

  def existsStub = mockAlgebra.doesAirlineExist _
  def getByIdStub = mockAlgebra.getAirline _
  def createStub = mockAlgebra.createAirline _
  def updateStub = mockAlgebra.updateAirline _
  def patchStub = mockAlgebra.partiallyUpdateAirline _
  def removeStub = mockAlgebra.removeAirline _

  def armGetAll(): Unit = (() => mockAlgebra.getAirlines).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirlinesBy).when().returns(mockGetBy)

  private def armGetByCountry(): Unit =
    (() => mockAlgebra.getAirlinesByCountry).when().returns(mockGetBy)

  val sampleCreate: AirlineCreate = AirlineCreate("Indigo", "6E", "IGO", "IFLY", 1)
  def fromCreate(id: Long, create: AirlineCreate): Airline = Airline.fromCreate(id, create)
  def withCreateId(create: AirlineCreate, id: Long): AirlineCreate = create.copy(id = Some(id))
  val samplePatch: AirlinePatch = AirlinePatch(name = Some("IndiGo Airlines"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("country", () => armGetByCountry(), countryFixtures)
  testCrudBehavior()
}
