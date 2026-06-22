package flightdatabase.api.endpoints

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import flightdatabase._
import flightdatabase.language.Language
import flightdatabase.language.LanguageAlgebra
import flightdatabase.language.LanguageCreate
import flightdatabase.language.LanguagePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.testutils.endpoints.EntityEndpointsSpec
import flightdatabase.testutils.endpoints.FieldFixture
import io.circe.Decoder
import io.circe.Encoder

final class LanguageEndpointsTest
    extends EntityEndpointsSpec[Language, LanguageCreate, LanguagePatch] {

  val mockAlgebra: LanguageAlgebra[IO] = stub[LanguageAlgebra[IO]]
  override val api: Endpoints[IO] = LanguageEndpoints[IO]("/languages", mockAlgebra)

  override val mockGetAll = stub[PartiallyAppliedGetAll[IO, Language]]
  override val mockGetBy = stub[PartiallyAppliedGetBy[IO, Language]]

  val table: TableBase[Language] = Language.languageTableBase
  val modelDecoder: Decoder[Language] = Decoder[Language]
  val modelEncoder: Encoder[Language] = Encoder[Language]
  val createEncoder: Encoder[LanguageCreate] = Encoder[LanguageCreate]
  val patchEncoder: Encoder[LanguagePatch] = Encoder[LanguagePatch]

  // Mirrors `originalLanguages` in LanguageRepositoryIT.
  val samples: Nel[Language] = Nel.of(
    Language(1, "English", "EN", Some("ENG"), "English"),
    Language(2, "German", "DE", Some("DEU"), "Deutsch"),
    Language(3, "Tamil", "TA", Some("TAM"), "Tamil"),
    Language(4, "Swedish", "SV", Some("SWE"), "Svenska"),
    Language(5, "Arabic", "AR", Some("ARA"), "Al-Arabiyyah"),
    Language(6, "Dutch", "NL", Some("NLD"), "Nederlands"),
    Language(7, "Hindi", "HI", Some("HIN"), "Hindi")
  )

  val fieldFixtures: List[FieldFixture[_]] = List(
    FieldFixture("name", "German", Operator.Equals, StringType),
    FieldFixture("iso2", "DE", Operator.NotIn, StringType),
    FieldFixture("id", 1L, Operator.In, LongType)
  )

  def existsStub = mockAlgebra.doesLanguageExist _
  def getByIdStub = mockAlgebra.getLanguage _
  def createStub = mockAlgebra.createLanguage _
  def updateStub = mockAlgebra.updateLanguage _
  def patchStub = mockAlgebra.partiallyUpdateLanguage _
  def removeStub = mockAlgebra.removeLanguage _

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
