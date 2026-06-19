package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.country.Country
import flightdatabase.country.CountryAlgebra
import flightdatabase.country.CountryCreate
import flightdatabase.country.CountryPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class CountryEndpointsTest extends EntityEndpointsSpec[Country, CountryCreate, CountryPatch] {

  val mockAlgebra: CountryAlgebra[IO] = stub[CountryAlgebra[IO]]
  override val api: Endpoints[IO] = CountryEndpoints[IO]("/countries", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, Country]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, Country]]

  val table: TableBase[Country] = Country.countryTableBase
  val modelDecoder: Decoder[Country] = Decoder[Country]
  val modelEncoder: Encoder[Country] = Encoder[Country]
  val createEncoder: Encoder[CountryCreate] = Encoder[CountryCreate]
  val patchEncoder: Encoder[CountryPatch] = Encoder[CountryPatch]

  // Mirrors `originalCountries` in CountryRepositoryIT.
  val samples: Nel[Country] = Nel.of(
    Country(1, "India", "IN", "IND", 91, Some(".in"), 7, Some(1), Some(3), 1, "Indian"),
    Country(2, "Germany", "DE", "DEU", 49, Some(".de"), 2, None, None, 2, "German"),
    Country(3, "Sweden", "SE", "SWE", 46, Some(".se"), 4, None, None, 3, "Swede"),
    Country(
      4,
      "United Arab Emirates",
      "AE",
      "ARE",
      971,
      Some(".ae"),
      5,
      Some(1),
      None,
      4,
      "Emirati"
    ),
    Country(5, "Netherlands", "NL", "NLD", 31, Some(".nl"), 6, None, None, 2, "Dutch"),
    Country(
      6,
      "United States of America",
      "US",
      "USA",
      1,
      Some(".us"),
      1,
      None,
      None,
      5,
      "US citizen"
    )
  )

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

  def existsStub = mockAlgebra.doesCountryExist _
  def getByIdStub = mockAlgebra.getCountry _
  def createStub = mockAlgebra.createCountry _
  def updateStub = mockAlgebra.updateCountry _
  def patchStub = mockAlgebra.partiallyUpdateCountry _
  def removeStub = mockAlgebra.removeCountry _

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
