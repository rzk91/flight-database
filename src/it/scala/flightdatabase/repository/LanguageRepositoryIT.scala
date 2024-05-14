package flightdatabase.repository

import cats.data.{NonEmptyList => Nel}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.InvalidField
import flightdatabase.domain.InvalidValueType
import flightdatabase.domain.SqlError
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageCreate
import flightdatabase.domain.language.LanguagePatch
import flightdatabase.testutils.RepositoryCheck
import flightdatabase.testutils.implicits._
import org.scalatest.Inspectors.forAll

final class LanguageRepositoryIT extends RepositoryCheck {

  lazy val repo: LanguageRepository[IO] = LanguageRepository.make[IO].unsafeRunSync()

  val originalLanguages: Nel[Language] = Nel.of(
    Language(1, "English", "EN", Some("ENG"), "English"),
    Language(2, "German", "DE", Some("DEU"), "Deutsch"),
    Language(3, "Tamil", "TA", Some("TAM"), "Tamil"),
    Language(4, "Swedish", "SV", Some("SWE"), "Svenska"),
    Language(5, "Arabic", "AR", Some("ARA"), "Al-Arabiyyah"),
    Language(6, "Dutch", "NL", Some("NLD"), "Nederlands"),
    Language(7, "Hindi", "HI", Some("HIN"), "Hindi")
  )

  val idNotPresent: Long = 100
  val valueNotPresent: String = "Not present"
  val veryLongIdNotPresent: Long = 1000000000000000000L
  val sqlErrorStringTooLong: SqlError = SqlError("22001")
  val invalidFieldSyntax: String = "Field with spaces"
  val sqlErrorInvalidSyntax: SqlError = SqlError("42601")
  val invalidFieldColumn: String = "non_existent_field"
  val invalidStringValue: Int = 1

  val newLanguage: LanguageCreate =
    LanguageCreate("New Language", "NA", Some("NLA"), "New Language")
  val updatedName: String = "Updated Language"
  val patchedName: String = "Patched Language"

  "Checking if a language exists" should "return a valid result" in {
    def languageExists(id: Long): Boolean = repo.doesLanguageExist(id).unsafeRunSync()
    languageExists(1) shouldBe true
    languageExists(idNotPresent) shouldBe false
    languageExists(veryLongIdNotPresent) shouldBe false
  }

  "Selecting all languages" should "return the correct detailed list" in {
    repo.getLanguages.value should contain only (originalLanguages.toList: _*)
  }

  it should "only return names if so required" in {
    repo
      .getLanguagesOnly[String]("name")
      .value should contain only (originalLanguages.map(_.name).toList: _*)
    repo
      .getLanguagesOnly[String]("iso2")
      .value should contain only (originalLanguages.map(_.iso2).toList: _*)
  }

  "Selecting a language by ID" should "return the correct language" in {
    forAll(originalLanguages)(language => repo.getLanguage(language.id).value shouldBe language)
    repo.getLanguage(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
    repo.getLanguage(veryLongIdNotPresent).error shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a language by other fields" should "return the corresponding entries" in {
    def languageByName(name: String): IO[ApiResult[Nel[Language]]] =
      repo.getLanguagesBy("name", Nel.one(name), Operator.Equals)

    def languageByIso2(iso2: String): IO[ApiResult[Nel[Language]]] =
      repo.getLanguagesBy("iso2", Nel.one(iso2), Operator.Equals)

    def languageByIso3(iso3: String): IO[ApiResult[Nel[Language]]] =
      repo.getLanguagesBy("iso3", Nel.one(iso3), Operator.Equals)

    def languageByOriginalName(name: String): IO[ApiResult[Nel[Language]]] =
      repo.getLanguagesBy("original_name", Nel.one(name), Operator.Equals)

    forAll(originalLanguages) { language =>
      languageByName(language.name).value should contain only language
      languageByIso2(language.iso2).value should contain only language
      language.iso3.foreach(iso3 => languageByIso3(iso3).value should contain only language)
      languageByOriginalName(language.originalName).value should contain(language)
    }

    languageByName(valueNotPresent).error shouldBe EntryListEmpty
    languageByIso2(valueNotPresent).error shouldBe EntryListEmpty
    languageByIso3(valueNotPresent).error shouldBe EntryListEmpty
    languageByOriginalName(valueNotPresent).error shouldBe EntryListEmpty
  }

  "Selecting a non-existent field" should "return an error" in {
    repo
      .getLanguagesBy(invalidFieldSyntax, Nel.one("value"), Operator.Equals)
      .error shouldBe sqlErrorInvalidSyntax
    repo
      .getLanguagesBy(invalidFieldColumn, Nel.one("value"), Operator.Equals)
      .error shouldBe InvalidField(invalidFieldColumn)
  }

  "Selecting an existing field with an invalid value type" should "return an error" in {
    repo
      .getLanguagesBy("name", Nel.one(invalidStringValue), Operator.Equals)
      .error shouldBe InvalidValueType(
      invalidStringValue.toString
    )
  }

  "Creating a new language" should "not take place if fields do not satisfy their criteria" in {
    val invalidLanguages = List(
      newLanguage.copy(name = ""),
      newLanguage.copy(iso2 = ""),
      newLanguage.copy(iso3 = Some("")),
      newLanguage.copy(originalName = "")
    )

    forAll(invalidLanguages) { language =>
      repo.createLanguage(language).error shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      newLanguage.copy(iso2 = "ENG"),          // Must be 2 characters
      newLanguage.copy(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo.createLanguage(language).error shouldBe sqlErrorStringTooLong
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      newLanguage.copy(iso2 = existingLanguage.iso2),
      newLanguage.copy(iso3 = existingLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo.createLanguage(language).error shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val newLanguageId = repo.createLanguage(newLanguage).value
    val newLanguageFromDb = repo.getLanguage(newLanguageId).value

    newLanguageFromDb shouldBe Language.fromCreate(newLanguageId, newLanguage)
  }

  it should "throw a conflict error if we create the same language again" in {
    repo.createLanguage(newLanguage).error shouldBe EntryAlreadyExists
  }

  "Updating a language" should "not take place if fields do not satisfy their criteria" in {
    val existingLanguage = originalLanguages.head

    val invalidLanguages = List(
      existingLanguage.copy(name = ""),
      existingLanguage.copy(iso2 = ""),
      existingLanguage.copy(iso3 = Some("")),
      existingLanguage.copy(originalName = "")
    )

    forAll(invalidLanguages) { language =>
      repo.updateLanguage(language).error shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      existingLanguage.copy(iso2 = "ENG"),          // Must be 2 characters
      existingLanguage.copy(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo.updateLanguage(language).error shouldBe sqlErrorStringTooLong
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      existingLanguage.copy(iso2 = newLanguage.iso2),
      existingLanguage.copy(iso3 = newLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo.updateLanguage(language).error shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we update a language that does not exist" in {
    val updated = Language.fromCreate(idNotPresent, newLanguage)
    repo.updateLanguage(updated).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingLanguage =
      repo.getLanguagesBy("name", Nel.one(newLanguage.name), Operator.Equals).value.head
    val updated = existingLanguage.copy(name = updatedName)
    repo.updateLanguage(updated).value shouldBe existingLanguage.id

    val updatedLanguageFromDb = repo.getLanguage(existingLanguage.id).value
    updatedLanguageFromDb shouldBe updated
  }

  "Patching a language" should "not take place if fields do not satisfy their criteria" in {
    val existingLanguage = originalLanguages.head

    val invalidLanguages = List(
      LanguagePatch(name = Some("")),
      LanguagePatch(iso2 = Some("")),
      LanguagePatch(iso3 = Some("")),
      LanguagePatch(originalName = Some(""))
    )

    forAll(invalidLanguages) { language =>
      repo.partiallyUpdateLanguage(existingLanguage.id, language).error shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      LanguagePatch(iso2 = Some("ENG")),    // Must be 2 characters
      LanguagePatch(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo
        .partiallyUpdateLanguage(existingLanguage.id, language)
        .error shouldBe sqlErrorStringTooLong
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      LanguagePatch(iso2 = Some(newLanguage.iso2)),
      LanguagePatch(iso3 = newLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo.partiallyUpdateLanguage(existingLanguage.id, language).error shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we patch a language that does not exist" in {
    val patched = LanguagePatch(name = Some(patchedName))
    repo.partiallyUpdateLanguage(idNotPresent, patched).error shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingLanguage =
      repo.getLanguagesBy("name", Nel.one(updatedName), Operator.Equals).value.head
    val patched = LanguagePatch(name = Some(patchedName))

    repo
      .partiallyUpdateLanguage(existingLanguage.id, patched)
      .value shouldBe existingLanguage.copy(name = patchedName)

    val patchedLanguageFromDb = repo.getLanguage(existingLanguage.id).value
    patchedLanguageFromDb shouldBe existingLanguage.copy(name = patchedName)
  }

  "Removing a language" should "work correctly" in {
    val existingLanguage =
      repo.getLanguagesBy("name", Nel.one(patchedName), Operator.Equals).value.head
    repo.removeLanguage(existingLanguage.id).value shouldBe ()

    repo.getLanguage(existingLanguage.id).error shouldBe EntryNotFound(existingLanguage.id)
  }

  it should "not work if the language does not exist" in {
    repo.removeLanguage(idNotPresent).error shouldBe EntryNotFound(idNotPresent)
  }
}
