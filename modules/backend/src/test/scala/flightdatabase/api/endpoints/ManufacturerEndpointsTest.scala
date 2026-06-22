package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.manufacturer.Manufacturer
import flightdatabase.manufacturer.ManufacturerAlgebra
import flightdatabase.manufacturer.ManufacturerCreate
import flightdatabase.manufacturer.ManufacturerPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class ManufacturerEndpointsTest
    extends EntityEndpointsSpec[Manufacturer, ManufacturerCreate, ManufacturerPatch] {

  val mockAlgebra: ManufacturerAlgebra[IO] = stub[ManufacturerAlgebra[IO]]
  override val api: Endpoints[IO] = ManufacturerEndpoints[IO]("/manufacturers", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, Manufacturer]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, Manufacturer]]

  val table: TableBase[Manufacturer] = Manufacturer.manufacturerTableBase
  val modelDecoder: Decoder[Manufacturer] = Decoder[Manufacturer]
  val modelEncoder: Encoder[Manufacturer] = Encoder[Manufacturer]
  val createEncoder: Encoder[ManufacturerCreate] = Encoder[ManufacturerCreate]
  val patchEncoder: Encoder[ManufacturerPatch] = Encoder[ManufacturerPatch]

  // Mirrors `originalManufacturers` in ManufacturerRepositoryIT.
  val samples: Nel[Manufacturer] = Nel.of(
    Manufacturer(1, "Airbus", 5),
    Manufacturer(2, "Boeing", 6)
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Airbus", Operator.Equals, StringType),
    FieldFixture("base_city_id", 5L, Operator.In, LongType)
  )

  // City is the external table reachable via /manufacturers/city/filter.
  private val cityFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Berlin", Operator.Equals, StringType),
    FieldFixture("capital", true, Operator.Is, BooleanType),
    FieldFixture("population", 1000000L, Operator.GreaterThan, LongType),
    FieldFixture("latitude", BigDecimal("48.14"), Operator.LessThan, BigDecimalType)
  )

  // Country is the external table reachable via /manufacturers/country/filter.
  private val countryFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Germany", Operator.Equals, StringType),
    FieldFixture("country_code", 49, Operator.GreaterThan, IntType)
  )

  def existsStub = mockAlgebra.doesManufacturerExist _
  def getByIdStub = mockAlgebra.getManufacturer _
  def createStub = mockAlgebra.createManufacturer _
  def updateStub = mockAlgebra.updateManufacturer _
  def patchStub = mockAlgebra.partiallyUpdateManufacturer _
  def removeStub = mockAlgebra.removeManufacturer _

  def armGetAll(): Unit = (() => mockAlgebra.getManufacturers).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getManufacturersBy).when().returns(mockGetBy)

  private def armGetByCity(): Unit =
    (() => mockAlgebra.getManufacturersByCity).when().returns(mockGetBy)

  private def armGetByCountry(): Unit =
    (() => mockAlgebra.getManufacturersByCountry).when().returns(mockGetBy)

  val sampleCreate: ManufacturerCreate = ManufacturerCreate("ADA", 1)

  def fromCreate(id: Long, create: ManufacturerCreate): Manufacturer =
    Manufacturer.fromCreate(id, create)

  def withCreateId(create: ManufacturerCreate, id: Long): ManufacturerCreate =
    create.copy(id = Some(id))

  val samplePatch: ManufacturerPatch =
    ManufacturerPatch(name = Some("Aeronautical Development Agency"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("city", () => armGetByCity(), cityFixtures)
  testExternalFilterBehavior("country", () => armGetByCountry(), countryFixtures)
  testCrudBehavior()
}
