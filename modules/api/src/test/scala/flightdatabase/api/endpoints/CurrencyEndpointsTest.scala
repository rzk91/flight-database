package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.api.testutils.endpoints._
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyAlgebra
import flightdatabase.currency.CurrencyCreate
import flightdatabase.currency.CurrencyPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class CurrencyEndpointsTest
    extends EntityEndpointsSpec[Currency, CurrencyCreate, CurrencyPatch] {

  val mockAlgebra: CurrencyAlgebra[IO] = stub[CurrencyAlgebra[IO]]
  override val api: Endpoints[IO] = CurrencyEndpoints[IO]("/currencies", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, Currency] =
    stub[flightdatabase.partial.PartiallyAppliedGetAll[IO, Currency]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, Currency] =
    stub[flightdatabase.partial.PartiallyAppliedGetBy[IO, Currency]]

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

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesCurrencyExist _
  def getByIdStub: StubFunction1[Long, IO[ApiResult[Currency]]] = mockAlgebra.getCurrency _
  def createStub: StubFunction1[CurrencyCreate, IO[ApiResult[Long]]] = mockAlgebra.createCurrency _
  def updateStub: StubFunction1[Currency, IO[ApiResult[Long]]] = mockAlgebra.updateCurrency _

  def patchStub: StubFunction2[Long, CurrencyPatch, IO[ApiResult[Currency]]] =
    mockAlgebra.partiallyUpdateCurrency _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeCurrency _

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
