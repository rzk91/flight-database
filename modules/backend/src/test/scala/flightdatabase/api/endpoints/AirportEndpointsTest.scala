package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase.ApiResult
import flightdatabase._
import flightdatabase.airport.Airport
import flightdatabase.airport.AirportAlgebra
import flightdatabase.airport.AirportCreate
import flightdatabase.airport.AirportPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class AirportEndpointsTest extends EntityEndpointsSpec[Airport, AirportCreate, AirportPatch] {

  val mockAlgebra: AirportAlgebra[IO] = stub[AirportAlgebra[IO]]
  override val api: Endpoints[IO] = AirportEndpoints[IO]("/airports", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, Airport] =
    stub[PartiallyAppliedGetAll[IO, Airport]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, Airport] =
    stub[PartiallyAppliedGetBy[IO, Airport]]

  val table: TableBase[Airport] = Airport.airportTableBase
  val modelDecoder: Decoder[Airport] = Decoder[Airport]
  val modelEncoder: Encoder[Airport] = Encoder[Airport]
  val createEncoder: Encoder[AirportCreate] = Encoder[AirportCreate]
  val patchEncoder: Encoder[AirportPatch] = Encoder[AirportPatch]

  // Mirrors `originalAirports` in AirportRepositoryIT.
  val samples: Nel[Airport] = Nel.of(
    Airport(
      1,
      "Frankfurt am Main Airport",
      "EDDF",
      "FRA",
      2,
      4,
      3,
      65000000,
      international = true,
      junction = true
    ),
    Airport(
      2,
      "Kempegowda International Airport",
      "VOBL",
      "BLR",
      1,
      2,
      2,
      16800000,
      international = true,
      junction = false
    ),
    Airport(
      3,
      "Dubai International Airport",
      "OMDB",
      "DXB",
      4,
      2,
      3,
      92500000,
      international = true,
      junction = false
    )
  )

  // Covers every field-type dispatch branch: String, Int, Long, and the Boolean-only `is` operator.
  // Uses the space-free `icao` for the String branch (names contain spaces that the raw query
  // string cannot carry).
  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("icao", "EDDF", Operator.Equals, StringType),
    FieldFixture("iata", "FRA", Operator.NotIn, StringType),
    FieldFixture("number_of_runways", 2, Operator.GreaterThan, IntType),
    FieldFixture("capacity", 1000000L, Operator.GreaterThan, LongType),
    FieldFixture("international", true, Operator.Is, BooleanType)
  )

  // City is the external table reachable via /airports/city/filter.
  private val cityFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Berlin", Operator.Equals, StringType),
    FieldFixture("capital", true, Operator.Is, BooleanType),
    FieldFixture("population", 1000000L, Operator.GreaterThan, LongType),
    FieldFixture("latitude", BigDecimal("48.14"), Operator.LessThan, BigDecimalType)
  )

  // Country is the external table reachable via /airports/country/filter.
  private val countryFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Germany", Operator.Equals, StringType),
    FieldFixture("country_code", 49, Operator.GreaterThan, IntType)
  )

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesAirportExist _
  def getByIdStub: StubFunction1[Long, IO[ApiResult[Airport]]] = mockAlgebra.getAirport _
  def createStub: StubFunction1[AirportCreate, IO[ApiResult[Long]]] = mockAlgebra.createAirport _
  def updateStub: StubFunction1[Airport, IO[ApiResult[Long]]] = mockAlgebra.updateAirport _

  def patchStub: StubFunction2[Long, AirportPatch, IO[ApiResult[Airport]]] =
    mockAlgebra.partiallyUpdateAirport _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeAirport _

  def armGetAll(): Unit = (() => mockAlgebra.getAirports).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getAirportsBy).when().returns(mockGetBy)

  private def armGetByCity(): Unit =
    (() => mockAlgebra.getAirportsByCity).when().returns(mockGetBy)

  private def armGetByCountry(): Unit =
    (() => mockAlgebra.getAirportsByCountry).when().returns(mockGetBy)

  val sampleCreate: AirportCreate = AirportCreate(
    "Chhatrapati Shivaji Maharaj International Airport",
    "VABB",
    "BOM",
    1,
    2,
    2,
    50000000,
    international = true,
    junction = false
  )
  def fromCreate(id: Long, create: AirportCreate): Airport = Airport.fromCreate(id, create)
  def withCreateId(create: AirportCreate, id: Long): AirportCreate = create.copy(id = Some(id))

  val samplePatch: AirportPatch =
    AirportPatch(name = Some("Chhatrapati Shivaji Maharaj International Airport Patched"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("city", () => armGetByCity(), cityFixtures)
  testExternalFilterBehavior("country", () => armGetByCountry(), countryFixtures)
  testCrudBehavior()
}
