package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.FlightDbTable.CITY
import flightdatabase.domain.FlightDbTable.COUNTRY
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityModel
import flightdatabase.utils.implicits._

class CityRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends CityAlgebra[F] {

  override def getCities(maybeCountry: Option[String]): F[ApiResult[List[CityModel]]] =
    featureNotImplemented[F, List[CityModel]]

  override def getCitiesOnlyNames(maybeCountry: Option[String]): F[ApiResult[List[String]]] =
    getStringListBy(CITY, COUNTRY, maybeCountry).execute

  override def getCityById(id: Long): F[ApiResult[CityModel]] = featureNotImplemented[F, CityModel]

  override def createCity(city: CityModel): F[ApiResult[Long]] = featureNotImplemented[F, Long]

  override def updateCity(city: CityModel): F[ApiResult[CityModel]] =
    featureNotImplemented[F, CityModel]

  override def deleteCity(id: Long): F[ApiResult[CityModel]] = featureNotImplemented[F, CityModel]
}

object CityRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[CityRepository[F]] = new CityRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, CityRepository[F]] = Resource.pure(new CityRepository[F])
}
