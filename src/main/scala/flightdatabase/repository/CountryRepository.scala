package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository.queries.CountryQueries.deleteCountry
import flightdatabase.repository.queries.CountryQueries.insertCountry
import flightdatabase.repository.queries.CountryQueries.selectAllCountries
import flightdatabase.utils.implicits._

class CountryRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CountryAlgebra[F] {

  override def getCountries: F[ApiResult[List[CountryModel]]] =
    selectAllCountries.asList.execute

  override def getCountriesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[CountryModel, String]("name").execute

  override def getCountry(id: Long): F[ApiResult[CountryModel]] =
    featureNotImplemented[F, CountryModel]

  override def createCountry(country: CountryModel): F[ApiResult[Long]] =
    insertCountry(country).attemptInsert.execute

  override def updateCountry(country: CountryModel): F[ApiResult[CountryModel]] =
    featureNotImplemented[F, CountryModel]

  override def removeCountry(id: Long): F[ApiResult[Unit]] =
    deleteCountry(id).attemptDelete(id).execute
}

object CountryRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[CountryRepository[F]] =
    new CountryRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, CountryRepository[F]] =
    Resource.pure(new CountryRepository[F])
}
