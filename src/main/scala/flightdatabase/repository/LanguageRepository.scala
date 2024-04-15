package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.all._
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageModel
import flightdatabase.repository.queries.LanguageQueries.deleteLanguage
import flightdatabase.repository.queries.LanguageQueries.insertLanguage
import flightdatabase.repository.queries.LanguageQueries.selectAllLanguages
import flightdatabase.utils.implicits._

class LanguageRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends LanguageAlgebra[F] {

  override def getLanguages: F[ApiResult[List[LanguageModel]]] =
    selectAllLanguages.asList.execute

  override def getLanguagesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[LanguageModel, String]("name").execute

  override def getLanguage(id: Long): F[ApiResult[LanguageModel]] =
    featureNotImplemented[F, LanguageModel]

  override def createLanguage(language: LanguageModel): F[ApiResult[Long]] =
    insertLanguage(language).attemptInsert.execute

  override def updateLanguage(language: LanguageModel): F[ApiResult[LanguageModel]] =
    featureNotImplemented[F, LanguageModel]

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
