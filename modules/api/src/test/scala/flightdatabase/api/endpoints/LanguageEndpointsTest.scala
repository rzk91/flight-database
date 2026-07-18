package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.api.testutils.endpoints._
import flightdatabase.language.Language
import flightdatabase.language.LanguageAlgebra
import flightdatabase.language.LanguageCreate
import flightdatabase.language.LanguagePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.test.fixtures.language
import io.circe.Decoder
import io.circe.Encoder
import org.scalamock.function.StubFunction1
import org.scalamock.function.StubFunction2

final class LanguageEndpointsTest
    extends EntityEndpointsSpec[Language, LanguageCreate, LanguagePatch] {

  val mockAlgebra: LanguageAlgebra[IO] = stub[LanguageAlgebra[IO]]
  override val api: Endpoints[IO] = LanguageEndpoints[IO]("/languages", mockAlgebra)

  override val mockGetAll: PartiallyAppliedGetAll[IO, Language] =
    stub[PartiallyAppliedGetAll[IO, Language]]

  override val mockGetBy: PartiallyAppliedGetBy[IO, Language] =
    stub[PartiallyAppliedGetBy[IO, Language]]

  val table: TableBase[Language] = Language.languageTableBase
  val modelDecoder: Decoder[Language] = Decoder[Language]
  val modelEncoder: Encoder[Language] = Encoder[Language]
  val createEncoder: Encoder[LanguageCreate] = Encoder[LanguageCreate]
  val patchEncoder: Encoder[LanguagePatch] = Encoder[LanguagePatch]

  val samples: Nel[Language] = language.languages

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "German", Operator.Equals, StringType),
    FieldFixture("iso2", "DE", Operator.NotIn, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  def existsStub: StubFunction1[Long, IO[Boolean]] = mockAlgebra.doesLanguageExist _
  def getByIdStub: StubFunction1[Long, IO[ApiResult[Language]]] = mockAlgebra.getLanguage _
  def createStub: StubFunction1[LanguageCreate, IO[ApiResult[Long]]] = mockAlgebra.createLanguage _
  def updateStub: StubFunction1[Language, IO[ApiResult[Long]]] = mockAlgebra.updateLanguage _

  def patchStub: StubFunction2[Long, LanguagePatch, IO[ApiResult[Language]]] =
    mockAlgebra.partiallyUpdateLanguage _
  def removeStub: StubFunction1[Long, IO[ApiResult[Unit]]] = mockAlgebra.removeLanguage _

  def armGetAll(): Unit = (() => mockAlgebra.getLanguages).when().returns(mockGetAll)
  def armGetBy(): Unit = (() => mockAlgebra.getLanguagesBy).when().returns(mockGetBy)

  val sampleCreate: LanguageCreate =
    LanguageCreate("New Language", "NA", Some("NLA"), "New Language")
  def fromCreate(id: Long, create: LanguageCreate): Language = Language.fromCreate(id, create)
  def withCreateId(create: LanguageCreate, id: Long): LanguageCreate = create.copy(id = Some(id))
  val samplePatch: LanguagePatch = LanguagePatch(name = Some("Patched Language"))

  testExistenceBehavior()
  testGetByIdBehavior()
  testGetAllBehavior()
  testGetByFieldBehavior()
  testCrudBehavior()
}
