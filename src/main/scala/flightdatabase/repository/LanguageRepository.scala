package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.all._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.language.Language
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageCreate
import flightdatabase.domain.language.LanguagePatch
import flightdatabase.repository.queries.LanguageQueries.deleteLanguage
import flightdatabase.repository.queries.LanguageQueries.insertLanguage
import flightdatabase.repository.queries.LanguageQueries.languageExists
import flightdatabase.repository.queries.LanguageQueries.modifyLanguage
import flightdatabase.repository.queries.LanguageQueries.selectAllLanguages
import flightdatabase.repository.queries.LanguageQueries.selectLanguageBy
import flightdatabase.utils.implicits._

class LanguageRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends LanguageAlgebra[F] {

  override def doesLanguageExist(id: Long): F[Boolean] = languageExists(id).unique.execute

  override def getLanguages: F[ApiResult[Nel[Language]]] =
    selectAllLanguages.asNel().execute

  override def getLanguagesOnly[V: Read](field: String): F[ApiResult[Nel[V]]] =
    getFieldList[Language, V](field).execute

  override def getLanguage(id: Long): F[ApiResult[Language]] =
    selectLanguageBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getLanguagesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Language]]] =
    selectLanguageBy(field, values, operator).asNel(Some(field), Some(values)).execute

  override def createLanguage(language: LanguageCreate): F[ApiResult[Long]] =
    insertLanguage(language).attemptInsert.execute

  override def updateLanguage(language: Language): F[ApiResult[Long]] =
    modifyLanguage(language).attemptUpdate(language.id).execute

  override def partiallyUpdateLanguage(id: Long, patch: LanguagePatch): F[ApiResult[Language]] =
    EitherT(getLanguage(id)).flatMapF { languageOutput =>
      val language = languageOutput.value
      val patched = Language.fromPatch(id, patch, language)
      modifyLanguage(patched).attemptUpdate(patched).execute
    }.value

  override def removeLanguage(id: Long): F[ApiResult[Unit]] =
    deleteLanguage(id).attemptDelete(id).execute
}

object LanguageRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[LanguageRepository[F]] =
    new LanguageRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, LanguageRepository[F]] =
    Resource.pure(new LanguageRepository[F])
}
