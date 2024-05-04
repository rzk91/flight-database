package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineAlgebra
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.airline.AirlinePatch
import flightdatabase.repository.queries.AirlineQueries._
import flightdatabase.utils.implicits._

class AirlineRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirlineAlgebra[F] {

  override def doesAirlineExist(id: Long): F[Boolean] = airlineExists(id).unique.execute

  override def getAirlines: F[ApiResult[List[Airline]]] = selectAllAirlines.asList().execute

  override def getAirlinesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Airline, String]("name").execute

  override def getAirline(id: Long): F[ApiResult[Airline]] =
    selectAirlineBy("id", id).asSingle(id).execute

  override def getAirlines[V: Put](field: String, value: V): F[ApiResult[List[Airline]]] =
    selectAirlineBy(field, value).asList(Some(field), Some(value)).execute

  override def getAirlinesByCountry[V: Put](field: String, value: V): F[ApiResult[List[Airline]]] =
    selectAirlineByCountry[V](field, value).asList(Some(field), Some(value)).execute

  override def createAirline(airline: AirlineCreate): F[ApiResult[Long]] =
    insertAirline(airline).attemptInsert.execute

  override def updateAirline(airline: Airline): F[ApiResult[Long]] =
    modifyAirline(airline).attemptUpdate(airline.id).execute

  override def partiallyUpdateAirline(id: Long, patch: AirlinePatch): F[ApiResult[Airline]] =
    EitherT(getAirline(id)).flatMapF { airlineOutput =>
      val airline = airlineOutput.value
      val patched = Airline.fromPatch(id, patch, airline)
      modifyAirline(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirline(id: Long): F[ApiResult[Unit]] =
    deleteAirline(id).attemptDelete(id).execute
}

object AirlineRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirlineRepository[F]] =
    new AirlineRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirlineRepository[F]] =
    Resource.pure(new AirlineRepository[F])
}
