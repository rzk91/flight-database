package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyAlgebra
import flightdatabase.currency.CurrencyCreate
import flightdatabase.currency.CurrencyPatch
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class CurrencyEndpointsTest
    extends EntityEndpointsSpec[Currency, CurrencyCreate, CurrencyPatch] {

  val mockAlgebra: CurrencyAlgebra[IO] = stub[CurrencyAlgebra[IO]]
  override val api: Endpoints[IO] = CurrencyEndpoints[IO]("/currencies", mockAlgebra)

  override val mockGetAll = stub[flightdatabase.partial.PartiallyAppliedGetAll[IO, Currency]]
  override val mockGetBy = stub[flightdatabase.partial.PartiallyAppliedGetBy[IO, Currency]]

  val table: TableBase[Currency] = Currency.currencyTableBase
  val modelDecoder: Decoder[Currency] = Decoder[Currency]
  val modelEncoder: Encoder[Currency] = Encoder[Currency]
  val createEncoder: Encoder[CurrencyCreate] = Encoder[CurrencyCreate]
  val patchEncoder: Encoder[CurrencyPatch] = Encoder[CurrencyPatch]

  // Mirrors `originalCurrencies` in CurrencyRepositoryIT.
  val samples: Nel[Currency] = Nel.of(
    Currency(1, "Indian Rupee", "INR", Some("₹")),
    Currency(2, "Euro", "EUR", Some("€")),
    Currency(3, "Swedish Krona", "SEK", Some("kr")),
    Currency(4, "Dirham", "AED", None),
    Currency(5, "US Dollar", "USD", Some("$"))
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "Euro", Operator.Equals, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  def existsStub = mockAlgebra.doesCurrencyExist _
  def getByIdStub = mockAlgebra.getCurrency _
  def createStub = mockAlgebra.createCurrency _
  def updateStub = mockAlgebra.updateCurrency _
  def patchStub = mockAlgebra.partiallyUpdateCurrency _
  def removeStub = mockAlgebra.removeCurrency _

  def armGetAll(): Unit = (() => mockAlgebra.getCurrencies).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getCurrenciesBy).when().returns(mockGetBy)

  val sampleCreate: CurrencyCreate = CurrencyCreate("New Currency", "NCR", Some("NCR"))
  def fromCreate(id: Long, create: CurrencyCreate): Currency = Currency.fromCreate(id, create)
  def withCreateId(create: CurrencyCreate, id: Long): CurrencyCreate = create.copy(id = Some(id))
  val samplePatch: CurrencyPatch = CurrencyPatch(name = Some("Patched Currency"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testCrudBehavior()
}
