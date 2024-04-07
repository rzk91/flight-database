package flightdatabase.domain.language

import flightdatabase.domain.ApiResult

trait LanguageAlgebra[F[_]] {
  def getLanguages: F[ApiResult[List[LanguageModel]]]
  def getLanguagesOnlyNames: F[ApiResult[List[String]]]
  def getLanguage(id: Long): F[ApiResult[LanguageModel]]
  def createLanguage(language: LanguageModel): F[ApiResult[Long]]

  // TODO: How do we want to handle updates? Patches or full updates?
  def updateLanguage(language: LanguageModel): F[ApiResult[LanguageModel]]
  def removeLanguage(id: Long): F[ApiResult[Unit]]
}