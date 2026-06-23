package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.api.testutils.endpoints._
import flightdatabase.city.City
import flightdatabase.city.CityAlgebra
import flightdatabase.city.CityCreate
import flightdatabase.city.CityPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class CityEndpointsTest extends EntityEndpointsSpec[City, CityCreate, CityPatch] {

  val mockAlgebra: CityAlgebra[IO] = stub[CityAlgebra[IO]]
  override val api: Endpoints[IO] = CityEndpoints[IO]("/cities", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, City] =
    stub[flightdatabase.partial.PartiallyAppliedGetAll[IO, City]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, City] =
    stub[flightdatabase.partial.PartiallyAppliedGetBy[IO, City]]

  val table: TableBase[City] = City.cityTableBase
  val modelDecoder: Decoder[City] = Decoder[City]
  val modelEncoder: Encoder[City] = Encoder[City]
  val createEncoder: Encoder[CityCreate] = Encoder[CityCreate]
  val patchEncoder: Encoder[CityPatch] = Encoder[CityPatch]

  // Mirrors `originalCities` in CityRepositoryIT.
  val samples: Nel[City] = Nel.of(
    City(
      1,
      "Bangalore",
      1,
      capital = false,
      13193000,
      BigDecimal("12.978889"),
      BigDecimal("77.591667"),
      "Asia/Kolkata"
    ),
    City(
      2,
      "Frankfurt am Main",
      2,
      capital = false,
      791000,
      BigDecimal("50.110556"),
      BigDecimal("8.682222"),
      "Europe/Berlin"
    ),
    City(
      3,
      "Berlin",
      2,
      capital = true,
      3571000,
      BigDecimal("52.52"),
      BigDecimal("13.405"),
      "Europe/Berlin"
    ),
    City(
      4,
      "Dubai",
      4,
      capital = false,
      3490000,
      BigDecimal("23.5"),
      BigDecimal("54.5"),
      "Asia/Dubai"
    ),
    City(
      5,
      "Leiden",
      5,
      capital = false,
      125100,
      BigDecimal("52.16"),
      BigDecimal("4.49"),
      "Europe/Amsterdam"
    ),
    City(
      6,
      "Chicago",
      6,
      capital = false,
      8901000,
      BigDecimal("41.85003"),
      BigDecimal("-87.65005"),
      "America/Chicago"
    )
  )

  // Exercises every field-type dispatch branch, including the Boolean-only `is` operator and the
  // BigDecimal branch that no other entity in this slice reaches.
  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Berlin", Operator.Equals, StringType),
    FieldFixture("capital", true, Operator.Is, BooleanType),
    FieldFixture("population", 1000000L, Operator.GreaterThan, LongType),
    FieldFixture("latitude", BigDecimal("48.14"), Operator.LessThan, BigDecimalType)
  )

  private val countryFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Germany", Operator.Equals, StringType),
    FieldFixture("country_code", 49, Operator.GreaterThan, IntType)
  )

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesCityExist _
  def getByIdStub: StubFunction1[Long, IO[ApiResult[City]]] = mockAlgebra.getCity _
  def createStub: StubFunction1[CityCreate, IO[ApiResult[Long]]] = mockAlgebra.createCity _
  def updateStub: StubFunction1[City, IO[ApiResult[Long]]] = mockAlgebra.updateCity _

  def patchStub: StubFunction2[Long, CityPatch, IO[ApiResult[City]]] =
    mockAlgebra.partiallyUpdateCity _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeCity _

  def armGetAll(): Unit = (() => mockAlgebra.getCities).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getCitiesBy).when().returns(mockGetBy)

  private def armGetByCountry(): Unit =
    (() => mockAlgebra.getCitiesByCountry).when().returns(mockGetBy)

  val sampleCreate: CityCreate =
    CityCreate(
      "Munich",
      2,
      capital = false,
      1488000,
      BigDecimal("48.137222"),
      BigDecimal("11.575556"),
      "Europe/Berlin"
    )
  def fromCreate(id: Long, create: CityCreate): City = City.fromCreate(id, create)
  def withCreateId(create: CityCreate, id: Long): CityCreate = create.copy(id = Some(id))
  val samplePatch: CityPatch = CityPatch(name = Some("Minga"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("country", () => armGetByCountry(), countryFixtures)
  testCrudBehavior()
}
