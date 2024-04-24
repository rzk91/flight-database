package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryAlreadyExists
import flightdatabase.domain.EntryCheckFailed
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.EntryNotFound
import flightdatabase.domain.UnknownDbError
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageCreate
import flightdatabase.domain.language.LanguagePatch
import flightdatabase.testutils.RepositoryCheck
import org.scalatest.Inspectors.forAll

final class LanguageRepositoryIT extends RepositoryCheck {

  lazy val repo: LanguageRepository[IO] = LanguageRepository.make[IO].unsafeRunSync()

  val originalLanguages: List[Language] = List(
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
  val stringTooLongSqlState: String = "22001"

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
    val languages = repo.getLanguages.unsafeRunSync().value.value

    languages should not be empty
    languages should contain only (originalLanguages: _*)
  }

  it should "only return names if so required" in {
    val languageNames = repo.getLanguagesOnlyNames.unsafeRunSync().value.value

    languageNames should not be empty
    languageNames should contain only (originalLanguages.map(_.name): _*)
  }

  "Selecting a language by ID" should "return the correct language" in {
    def languageById(id: Long): ApiResult[Language] = repo.getLanguage(id).unsafeRunSync()

    forAll(originalLanguages) { language =>
      languageById(language.id).value.value shouldBe language
    }
    languageById(idNotPresent).left.value shouldBe EntryNotFound(idNotPresent)
    languageById(veryLongIdNotPresent).left.value shouldBe EntryNotFound(veryLongIdNotPresent)
  }

  "Selecting a language by other fields" should "return the corresponding entries" in {
    def languageByName(name: String): ApiResult[List[Language]] =
      repo.getLanguages("name", name).unsafeRunSync()

    def languageByIso2(iso2: String): ApiResult[List[Language]] =
      repo.getLanguages("iso2", iso2).unsafeRunSync()

    def languageByIso3(iso3: String): ApiResult[List[Language]] =
      repo.getLanguages("iso3", iso3).unsafeRunSync()

    def languageByOriginalName(originalName: String): ApiResult[List[Language]] =
      repo.getLanguages("original_name", originalName).unsafeRunSync()

    forAll(originalLanguages) { language =>
      languageByName(language.name).value.value should contain only language
      languageByIso2(language.iso2).value.value should contain only language
      language.iso3.foreach { iso3 =>
        languageByIso3(iso3).value.value should contain only language
      }
      languageByOriginalName(language.originalName).value.value should contain(language)
    }

    languageByName(valueNotPresent).left.value shouldBe EntryListEmpty
    languageByIso2(valueNotPresent).left.value shouldBe EntryListEmpty
    languageByIso3(valueNotPresent).left.value shouldBe EntryListEmpty
    languageByOriginalName(valueNotPresent).left.value shouldBe EntryListEmpty
  }

  "Creating a new language" should "not take place if fields do not satisfy their criteria" in {
    val invalidLanguages = List(
      newLanguage.copy(name = ""),
      newLanguage.copy(iso2 = ""),
      newLanguage.copy(iso3 = Some("")),
      newLanguage.copy(originalName = "")
    )

    forAll(invalidLanguages) { language =>
      repo.createLanguage(language).unsafeRunSync().left.value shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      newLanguage.copy(iso2 = "ENG"),          // Must be 2 characters
      newLanguage.copy(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo.createLanguage(language).unsafeRunSync().left.value shouldBe UnknownDbError(
        stringTooLongSqlState
      )
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      newLanguage.copy(iso2 = existingLanguage.iso2),
      newLanguage.copy(iso3 = existingLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo.createLanguage(language).unsafeRunSync().left.value shouldBe EntryAlreadyExists
    }
  }

  it should "work if all criteria are met" in {
    val newLanguageId = repo.createLanguage(newLanguage).unsafeRunSync().value.value
    val newLanguageFromDb = repo.getLanguage(newLanguageId).unsafeRunSync().value.value

    newLanguageFromDb shouldBe Language.fromCreate(newLanguageId, newLanguage)
  }

  it should "throw a conflict error if we create the same language again" in {
    repo.createLanguage(newLanguage).unsafeRunSync().left.value shouldBe EntryAlreadyExists
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
      repo.updateLanguage(language).unsafeRunSync().left.value shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      existingLanguage.copy(iso2 = "ENG"),          // Must be 2 characters
      existingLanguage.copy(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo.updateLanguage(language).unsafeRunSync().left.value shouldBe UnknownDbError(
        stringTooLongSqlState
      )
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      existingLanguage.copy(iso2 = newLanguage.iso2),
      existingLanguage.copy(iso3 = newLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo.updateLanguage(language).unsafeRunSync().left.value shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we update a language that does not exist" in {
    val updated = Language.fromCreate(idNotPresent, newLanguage)
    repo.updateLanguage(updated).unsafeRunSync().left.value shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingLanguage =
      repo.getLanguages("name", newLanguage.name).unsafeRunSync().value.value.head
    val updated = existingLanguage.copy(name = updatedName)
    repo.updateLanguage(updated).unsafeRunSync().value.value shouldBe existingLanguage.id

    val updatedLanguageFromDb = repo.getLanguage(existingLanguage.id).unsafeRunSync().value.value
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
      repo
        .partiallyUpdateLanguage(existingLanguage.id, language)
        .unsafeRunSync()
        .left
        .value shouldBe EntryCheckFailed
    }

    val invalidLanguages2 = List(
      LanguagePatch(iso2 = Some("ENG")),    // Must be 2 characters
      LanguagePatch(iso3 = Some("ENGLISH")) // Must be 3 characters
    )

    forAll(invalidLanguages2) { language =>
      repo
        .partiallyUpdateLanguage(existingLanguage.id, language)
        .unsafeRunSync()
        .left
        .value shouldBe UnknownDbError(stringTooLongSqlState)
    }
  }

  it should "not take place for a language with existing unique fields" in {
    val existingLanguage = originalLanguages.head

    val duplicateLanguages = List(
      LanguagePatch(iso2 = Some(newLanguage.iso2)),
      LanguagePatch(iso3 = newLanguage.iso3)
    )

    forAll(duplicateLanguages) { language =>
      repo
        .partiallyUpdateLanguage(existingLanguage.id, language)
        .unsafeRunSync()
        .left
        .value shouldBe EntryAlreadyExists
    }
  }

  it should "throw an error if we patch a language that does not exist" in {
    val patched = LanguagePatch(name = Some(patchedName))
    repo
      .partiallyUpdateLanguage(idNotPresent, patched)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(idNotPresent)
  }

  it should "work if all criteria are met" in {
    val existingLanguage = repo.getLanguages("name", updatedName).unsafeRunSync().value.value.head
    val patched = LanguagePatch(name = Some(patchedName))

    repo
      .partiallyUpdateLanguage(existingLanguage.id, patched)
      .unsafeRunSync()
      .value
      .value shouldBe existingLanguage.copy(name = patchedName)

    val patchedLanguageFromDb = repo.getLanguage(existingLanguage.id).unsafeRunSync().value.value
    patchedLanguageFromDb shouldBe existingLanguage.copy(name = patchedName)
  }

  "Removing a language" should "work correctly" in {
    val existingLanguage = repo.getLanguages("name", patchedName).unsafeRunSync().value.value.head
    repo.removeLanguage(existingLanguage.id).unsafeRunSync().value.value shouldBe ()

    repo.getLanguage(existingLanguage.id).unsafeRunSync().left.value shouldBe EntryNotFound(
      existingLanguage.id
    )
  }

  it should "not work if the language does not exist" in {
    repo
      .removeLanguage(idNotPresent)
      .unsafeRunSync()
      .left
      .value shouldBe EntryNotFound(idNotPresent)
  }
}
