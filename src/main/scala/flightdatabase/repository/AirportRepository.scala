package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.airport.AirportPatch
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.listToApiResult
import flightdatabase.repository.queries.AirportQueries._
import flightdatabase.utils.FieldValue
import flightdatabase.utils.implicits._

class AirportRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirportAlgebra[F] {

  override def doesAirportExist(id: Long): F[Boolean] = airportExists(id).unique.execute

  override def getAirports: F[ApiResult[List[Airport]]] =
    selectAllAirports.asList.execute

  override def getAirportsOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Airport, String]("name").execute

  override def getAirport(id: Long): F[ApiResult[Airport]] =
    selectAirportsBy("id", id).asSingle(id).execute

  override def getAirportsBy[V: Put](field: String, value: V): F[ApiResult[List[Airport]]] =
    selectAirportsBy(field, value).asList.execute

  override def getAirportsByCity(city: String): F[ApiResult[List[Airport]]] =
    selectAllAirportsByExternal[City, String]("name", city).asList.execute

  override def getAirportsByCountry(country: String): F[ApiResult[List[Airport]]] =
    EitherT(getFieldList[City, String, Country, String]("name", FieldValue("name", country)))
      .flatMapF {
        _.value
          .flatTraverse(selectAllAirportsByExternal[City, String]("name", _).to[List])
          .map(listToApiResult)
      }
      .value
      .execute

  override def createAirport(airport: AirportCreate): F[ApiResult[Long]] =
    insertAirport(airport).attemptInsert.execute

  override def updateAirport(airport: Airport): F[ApiResult[Airport]] =
    modifyAirport(airport).attemptUpdate(airport).execute

  override def partiallyUpdateAirport(id: Long, patch: AirportPatch): F[ApiResult[Airport]] =
    EitherT(getAirport(id)).flatMapF { airportOutput =>
      val airport = airportOutput.value
      updateAirport(Airport.fromPatch(id, patch, airport))
    }.value

  override def removeAirport(id: Long): F[ApiResult[Unit]] =
    deleteAirport(id).attemptDelete(id).execute
}

object AirportRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirportRepository[F]] =
    new AirportRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirportRepository[F]] =
    Resource.pure(new AirportRepository[F])
}
