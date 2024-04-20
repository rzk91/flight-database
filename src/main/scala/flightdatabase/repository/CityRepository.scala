package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityCreate
import flightdatabase.domain.city.CityPatch
import flightdatabase.domain.country.Country
import flightdatabase.repository.queries.CityQueries._
import flightdatabase.utils.implicits._

class CityRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CityAlgebra[F] {

  override def doesCityExist(id: Long): F[Boolean] = cityExists(id).unique.execute

  override def getCities: F[ApiResult[List[City]]] = selectAllCities.asList.execute

  override def getCitiesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[City, String]("name").execute

  override def getCity(id: Long): F[ApiResult[City]] = selectCitiesBy("id", id).asSingle(id).execute

  override def getCities[V: Put](field: String, value: V): F[ApiResult[List[City]]] =
    selectCitiesBy(field, value).asList.execute

  override def getCitiesByCountry(country: String): F[ApiResult[List[City]]] =
    selectCitiesByExternal[Country, String]("name", country).asList.execute

  // TODO: Add checks for latitude/longitude <-> country matching
  //  and for timezone validity
  //  and for timezone <-> country matching
  override def createCity(city: CityCreate): F[ApiResult[Long]] =
    insertCity(city).attemptInsert.execute

  override def updateCity(city: City): F[ApiResult[Long]] =
    modifyCity(city).attemptUpdate(city.id).execute

  override def partiallyUpdateCity(id: Long, patch: CityPatch): F[ApiResult[City]] =
    EitherT(getCity(id)).flatMapF { cityOutput =>
      val city = cityOutput.value
      val patched = City.fromPatch(id, patch, city)
      modifyCity(patched).attemptUpdate(patched).execute
    }.value

  override def removeCity(id: Long): F[ApiResult[Unit]] =
    deleteCity(id).attemptDelete(id).execute
}

object CityRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[CityRepository[F]] = new CityRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, CityRepository[F]] = Resource.pure(new CityRepository[F])
}
