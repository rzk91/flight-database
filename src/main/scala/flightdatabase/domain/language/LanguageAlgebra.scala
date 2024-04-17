package flightdatabase.domain.language

import doobie.Put
import flightdatabase.domain.ApiResult

trait LanguageAlgebra[F[_]] {
  def doesLanguageExist(id: Long): F[Boolean]
  def getLanguages: F[ApiResult[List[Language]]]
  def getLanguagesOnlyNames: F[ApiResult[List[String]]]
  def getLanguage(id: Long): F[ApiResult[Language]]
  def getLanguages[V: Put](field: String, value: V): F[ApiResult[List[Language]]]
  def createLanguage(language: LanguageCreate): F[ApiResult[Long]]
  def updateLanguage(language: Language): F[ApiResult[Language]]
  def partiallyUpdateLanguage(id: Long, patch: LanguagePatch): F[ApiResult[Language]]
  def removeLanguage(id: Long): F[ApiResult[Unit]]
}
