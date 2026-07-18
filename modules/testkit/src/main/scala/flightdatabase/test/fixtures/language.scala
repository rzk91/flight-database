package flightdatabase.test.fixtures

import cats.data.{NonEmptyList => Nel}
import flightdatabase.language.Language

trait LanguageFixtures {

  val languages: Nel[Language] = Nel.of(
    Language(1, "English", "EN", Some("ENG"), "English"),
    Language(2, "German", "DE", Some("DEU"), "Deutsch"),
    Language(3, "Tamil", "TA", Some("TAM"), "Tamil"),
    Language(4, "Swedish", "SV", Some("SWE"), "Svenska"),
    Language(5, "Arabic", "AR", Some("ARA"), "Al-Arabiyyah"),
    Language(6, "Dutch", "NL", Some("NLD"), "Nederlands"),
    Language(7, "Hindi", "HI", Some("HIN"), "Hindi")
  )
}

object language extends LanguageFixtures
