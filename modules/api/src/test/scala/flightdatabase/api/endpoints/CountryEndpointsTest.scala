package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.api.testutils.endpoints._
import flightdatabase.country.Country
import flightdatabase.country.CountryAlgebra
import flightdatabase.country.CountryCreate
import flightdatabase.country.CountryPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.test.fixtures
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class CountryEndpointsTest extends EntityEndpointsSpec[Country, CountryCreate, CountryPatch] {

  val mockAlgebra: CountryAlgebra[IO] = stub[CountryAlgebra[IO]]
  override val api: Endpoints[IO] = CountryEndpoints[IO]("/countries", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, Country] =
    stub[PartiallyAppliedGetAll[IO, Country]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, Country] =
    stub[PartiallyAppliedGetBy[IO, Country]]

  val table: TableBase[Country] = Country.countryTableBase
  val modelDecoder: Decoder[Country] = Decoder[Country]
  val modelEncoder: Encoder[Country] = Encoder[Country]
  val createEncoder: Encoder[CountryCreate] = Encoder[CountryCreate]
  val patchEncoder: Encoder[CountryPatch] = Encoder[CountryPatch]

  val samples: Nel[Country] = fixtures.countries

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Germany", Operator.Equals, StringType),
    FieldFixture("iso2", "DE", Operator.NotIn, StringType),
    FieldFixture("country_code", 49, Operator.GreaterThan, IntType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  // Language is the external table reachable via /countries/language/filter.
  private val languageFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "German", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  // Currency is the external table reachable via /countries/currency/filter.
  private val currencyFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Euro", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesCountryExist _
  def getByIdStub: StubFunction1[Long, IO[ApiResult[Country]]] = mockAlgebra.getCountry _
  def createStub: StubFunction1[CountryCreate, IO[ApiResult[Long]]] = mockAlgebra.createCountry _
  def updateStub: StubFunction1[Country, IO[ApiResult[Long]]] = mockAlgebra.updateCountry _

  def patchStub: StubFunction2[Long, CountryPatch, IO[ApiResult[Country]]] =
    mockAlgebra.partiallyUpdateCountry _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeCountry _

  def armGetAll(): Unit = (() => mockAlgebra.getCountries).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getCountriesBy).when().returns(mockGetBy)

  private def armGetByLanguage(): Unit =
    (() => mockAlgebra.getCountriesByLanguage).when().returns(mockGetBy)

  private def armGetByCurrency(): Unit =
    (() => mockAlgebra.getCountriesByCurrency).when().returns(mockGetBy)

  val sampleCreate: CountryCreate =
    CountryCreate("NewCountry", "NC", "NCT", 123, Some(".nc"), 5, Some(1), None, 4, "NewCountryian")
  def fromCreate(id: Long, create: CountryCreate): Country = Country.fromCreate(id, create)
  def withCreateId(create: CountryCreate, id: Long): CountryCreate = create.copy(id = Some(id))
  val samplePatch: CountryPatch = CountryPatch(name = Some("NewCountryPatched"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testExternalFilterBehavior("language", () => armGetByLanguage(), languageFixtures)
  testExternalFilterBehavior("currency", () => armGetByCurrency(), currencyFixtures)
  testCrudBehavior()
}
