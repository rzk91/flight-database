package flightdatabase.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import flightdatabase.domain.language.Language
import flightdatabase.testutils.RepositoryCheck

final class LanguageRepositoryIT extends RepositoryCheck {

  "Selecting all languages" should "return the correct detailed list" in {
    val languages = {
      for {
        repo         <- LanguageRepository.make[IO]
        allLanguages <- repo.getLanguages
      } yield allLanguages
    }.unsafeRunSync().value.value

    languages should not be empty
    languages should contain only (
      Language(1, "English", "EN", Some("ENG"), "English"),
      Language(2, "German", "DE", Some("DEU"), "Deutsch"),
      Language(3, "Tamil", "TA", Some("TAM"), "Tamil"),
      Language(4, "Swedish", "SV", Some("SWE"), "Svenska"),
      Language(5, "Arabic", "AR", Some("ARA"), "Al-Arabiyyah"),
      Language(6, "Dutch", "NL", Some("NLD"), "Nederlands"),
      Language(7, "Hindi", "HI", Some("HIN"), "Hindi")
    )
  }
}
