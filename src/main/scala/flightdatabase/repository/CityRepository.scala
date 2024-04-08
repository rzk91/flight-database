package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityModel
import flightdatabase.repository.queries.CityQueries._
import flightdatabase.utils.implicits._

class CityRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends CityAlgebra[F] {

  override def getCities: F[ApiResult[List[CityModel]]] = selectAllCities.asList.execute

  override def getCitiesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[CityModel, String]("name").execute

  override def getCity(id: Long): F[ApiResult[CityModel]] = featureNotImplemented[F, CityModel]

  override def getCitiesByCountry(country: String): F[ApiResult[List[CityModel]]] =
    selectAllCitiesByCountry(country).asList.execute

  override def createCity(city: CityModel): F[ApiResult[Long]] =
    insertCity(city).attemptInsert.execute

  override def updateCity(city: CityModel): F[ApiResult[CityModel]] =
    featureNotImplemented[F, CityModel]

  override def removeCity(id: Long): F[ApiResult[Unit]] =
    deleteCity(id).attemptDelete(id).execute
}

object CityRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[CityRepository[F]] = new CityRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, CityRepository[F]] = Resource.pure(new CityRepository[F])
}
