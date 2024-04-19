package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.country.Country
import flightdatabase.domain.country.CountryAlgebra
import flightdatabase.domain.country.CountryCreate
import flightdatabase.domain.country.CountryPatch
import flightdatabase.repository.queries.CountryQueries._
import flightdatabase.utils.implicits._

class CountryRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CountryAlgebra[F] {

  override def doesCountryExist(id: Long): F[Boolean] =
    countryExists(id).unique.execute

  override def getCountries: F[ApiResult[List[Country]]] =
    selectAllCountries.asList.execute

  override def getCountriesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Country, String]("name").execute

  override def getCountry(id: Long): F[ApiResult[Country]] =
    selectCountriesBy("id", id).asSingle(id).execute

  override def getCountries[V: Put](field: String, value: V): F[ApiResult[List[Country]]] =
    selectCountriesBy(field, value).asList.execute

  override def createCountry(country: CountryCreate): F[ApiResult[Long]] =
    insertCountry(country).attemptInsert.execute

  override def updateCountry(country: Country): F[ApiResult[Country]] =
    modifyCountry(country).attemptUpdate(country).execute

  override def partiallyUpdateCountry(
    id: Long,
    patch: CountryPatch
  ): F[ApiResult[Country]] =
    EitherT(getCountry(id)).flatMapF { countryOutput =>
      val country = countryOutput.value
      updateCountry(Country.fromPatch(id, patch, country))
    }.value

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
