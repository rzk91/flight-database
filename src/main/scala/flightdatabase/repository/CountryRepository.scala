package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.FlightDbTable.COUNTRY
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.country.CountryModel
import flightdatabase.utils.implicits._

class CountryRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends CountryAlgebra[F] {

  override def getCountries: F[ApiResult[List[CountryModel]]] =
    featureNotImplemented[F, List[CountryModel]]

  override def getCountriesOnlyNames: F[ApiResult[List[String]]] =
    getNameList(COUNTRY).execute

  override def getCountryById(id: Long): F[ApiResult[CountryModel]] =
    featureNotImplemented[F, CountryModel]

  override def createCountry(country: CountryModel): F[ApiResult[Long]] =
    featureNotImplemented[F, Long]

  override def updateCountry(country: CountryModel): F[ApiResult[CountryModel]] =
    featureNotImplemented[F, CountryModel]

  override def deleteCountry(id: Long): F[ApiResult[CountryModel]] =
    featureNotImplemented[F, CountryModel]
}

object CountryRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[CountryRepository[F]] =
    new CountryRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, CountryRepository[F]] =
    Resource.pure(new CountryRepository[F])
}
