package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.language.LanguageAlgebra
import flightdatabase.domain.language.LanguageModel
import flightdatabase.repository.queries.LanguageQueries.insertLanguage
import flightdatabase.repository.queries.LanguageQueries.selectAllLanguages
import flightdatabase.utils.implicits._

class LanguageRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends LanguageAlgebra[F] {

  // TODO: Perhaps move the actual queries to a separate object
  //    and call them from here
  //    - keep the repository clean
  //    - make it easier to test
  //    - make it easier to reuse the queries
  //    - make it easier to use either list or stream
  override def getLanguages: F[ApiResult[List[LanguageModel]]] =
    selectAllLanguages.asList.execute

  override def getLanguagesOnlyNames: F[ApiResult[List[String]]] =
    selectAllLanguages.map(_.name).asList.execute

  override def getLanguage(id: Long): F[ApiResult[LanguageModel]] =
    featureNotImplemented[F, LanguageModel]

  override def createLanguage(language: LanguageModel): F[ApiResult[Long]] =
    insertLanguage(language).insert.execute

  override def updateLanguage(language: LanguageModel): F[ApiResult[LanguageModel]] =
    featureNotImplemented[F, LanguageModel]

  override def deleteLanguage(id: Long): F[ApiResult[LanguageModel]] =
    featureNotImplemented[F, LanguageModel]
}

object LanguageRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[LanguageRepository[F]] =
    new LanguageRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, LanguageRepository[F]] =
    Resource.pure(new LanguageRepository[F])
}
