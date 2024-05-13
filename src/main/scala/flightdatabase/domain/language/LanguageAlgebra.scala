package flightdatabase.domain.language

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait LanguageAlgebra[F[_]] {
  def doesLanguageExist(id: Long): F[Boolean]
  def getLanguages: F[ApiResult[Nel[Language]]]
  def getLanguagesOnlyNames: F[ApiResult[Nel[String]]]
  def getLanguage(id: Long): F[ApiResult[Language]]

  def getLanguagesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Language]]]

  def createLanguage(language: LanguageCreate): F[ApiResult[Long]]
  def updateLanguage(language: Language): F[ApiResult[Long]]
  def partiallyUpdateLanguage(id: Long, patch: LanguagePatch): F[ApiResult[Language]]
  def removeLanguage(id: Long): F[ApiResult[Unit]]
}
