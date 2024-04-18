package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.all._
import doobie.Put
import doobie.Transactor
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

  override def getLanguages: F[ApiResult[List[Language]]] =
    selectAllLanguages.asList.execute

  override def getLanguagesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Language, String]("name").execute

  override def getLanguage(id: Long): F[ApiResult[Language]] =
    selectLanguageBy("id", id).asSingle(id).execute

  override def getLanguages[V: Put](field: String, value: V): F[ApiResult[List[Language]]] =
    selectLanguageBy(field, value).asList.execute

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
