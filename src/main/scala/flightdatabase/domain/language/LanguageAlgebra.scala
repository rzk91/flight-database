package flightdatabase.domain.language

trait LanguageAlgebra[F[_]] {
  def getLanguages: F[List[LanguageModel]]
  def getLanguagesOnlyNames: F[List[String]]
  def getLanguage(id: Long): F[Option[LanguageModel]]
  def createLanguage(language: LanguageModel): F[Long]

  // TODO: How do we want to handle updates? Patches or full updates?
  def updateLanguage(language: LanguageModel): F[Option[LanguageModel]]
  def deleteLanguage(id: Long): F[Option[LanguageModel]]
}
