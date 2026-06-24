package flightdatabase.language

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait LanguageAlgebra[F[_]] {
  def doesLanguageExist(id: Long): F[Boolean]
  def getLanguages: PartiallyAppliedGetAll[F, Language]
  def getLanguage(id: Long): F[ApiResult[Language]]
  def getLanguagesBy: PartiallyAppliedGetBy[F, Language]
  def createLanguage(language: LanguageCreate): F[ApiResult[Long]]
  def updateLanguage(language: Language): F[ApiResult[Long]]
  def partiallyUpdateLanguage(id: Long, patch: LanguagePatch): F[ApiResult[Language]]
  def removeLanguage(id: Long): F[ApiResult[Unit]]
}
