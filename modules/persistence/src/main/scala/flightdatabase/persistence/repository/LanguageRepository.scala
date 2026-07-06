package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.all._
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.language.Language
import flightdatabase.language.LanguageAlgebra
import flightdatabase.language.LanguageCreate
import flightdatabase.language.LanguagePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.LanguageRepository.PartiallyAppliedGetAllLanguages
import flightdatabase.persistence.repository.LanguageRepository.PartiallyAppliedGetByLanguage
import flightdatabase.persistence.repository.queries.LanguageQueries.deleteLanguage
import flightdatabase.persistence.repository.queries.LanguageQueries.insertLanguage
import flightdatabase.persistence.repository.queries.LanguageQueries.languageExists
import flightdatabase.persistence.repository.queries.LanguageQueries.modifyLanguage
import flightdatabase.persistence.repository.queries.LanguageQueries.selectAllLanguages
import flightdatabase.persistence.repository.queries.LanguageQueries.selectLanguageBy
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class LanguageRepository[F[_]: Concurrent] private (implicit
  transactor: Transactor[F]
) extends LanguageAlgebra[F] {

  override def doesLanguageExist(id: Long): F[Boolean] = languageExists(id).unique.execute

  override def getLanguages: PartiallyAppliedGetAll[F, Language] =
    new PartiallyAppliedGetAllLanguages[F]

  override def getLanguage(id: Long): F[ApiResult[Language]] =
    selectLanguageBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getLanguagesBy: PartiallyAppliedGetBy[F, Language] =
    new PartiallyAppliedGetByLanguage[F]

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

  def make[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): F[LanguageRepository[F]] =
    new LanguageRepository[F].pure[F]

  def resource[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): Resource[F, LanguageRepository[F]] =
    Resource.pure(new LanguageRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllLanguages[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Language] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Language]]] =
      selectAllLanguages(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Language, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByLanguage[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Language] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Language]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectLanguageBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
