package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.airport.AirportModel
import flightdatabase.domain.city.CityModel
import flightdatabase.domain.country.CountryModel
import flightdatabase.repository.queries.AirportQueries._
import flightdatabase.utils.TableValue
import flightdatabase.utils.implicits._

class AirportRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends AirportAlgebra[F] {

  override def getAirports: F[ApiResult[List[AirportModel]]] =
    selectAllAirports.asList.execute

  override def getAirportsOnlyNames: F[ApiResult[List[String]]] =
    getNameList[AirportModel].execute

  override def getAirport(id: Long): F[ApiResult[AirportModel]] =
    selectAirportBy("id", id).asSingle.execute

  override def getAirportByIata(iata: String): F[ApiResult[AirportModel]] =
    selectAirportBy("iata", iata).asSingle.execute

  override def getAirportByIcao(icao: String): F[ApiResult[AirportModel]] =
    selectAirportBy("icao", icao).asSingle.execute

  override def getAirportsByCity(city: String): F[ApiResult[List[AirportModel]]] =
    selectAllAirportsByExternal[CityModel, String]("name", city).asList.execute

  override def getAirportsByCountry(country: String): F[ApiResult[List[AirportModel]]] =
    getNameList[CityModel, CountryModel, String](Some(TableValue(country))).flatMap {
      case Left(error) => liftErrorToApiResult[List[AirportModel]](error).pure[ConnectionIO]
      case Right(cityNames) =>
        cityNames.value
          .flatTraverse(selectAllAirportsByExternal[CityModel, String]("name", _).to[List])
          .map(liftListToApiResult)
    }.execute

  override def createAirport(airport: AirportModel): F[ApiResult[Long]] =
    insertAirport(airport).attemptInsert.execute

  override def updateAirport(airport: AirportModel): F[ApiResult[AirportModel]] =
    featureNotImplemented[F, AirportModel]

  override def removeAirport(id: Long): F[ApiResult[Unit]] =
    deleteAirport(id).attemptDelete.execute
}

object AirportRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[AirportRepository[F]] =
    new AirportRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, AirportRepository[F]] =
    Resource.pure(new AirportRepository[F])
}
