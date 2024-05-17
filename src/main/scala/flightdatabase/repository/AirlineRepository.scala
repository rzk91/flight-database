package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.ValidatedSortAndLimit
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

  override def getAirlines(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Airline]]] =
    selectAllAirlines(sortAndLimit).asNel().execute

  override def getAirlinesOnly[V: Read](
    sortAndLimit: ValidatedSortAndLimit,
    returnField: String
  ): F[ApiResult[Nel[V]]] =
    getFieldList2[Airline, V](sortAndLimit, returnField).execute

  override def getAirline(id: Long): F[ApiResult[Airline]] =
    selectAirlineBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirlinesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airline]]] =
    selectAirlineBy(field, values, operator).asNel(Some(field), Some(values)).execute

  override def getAirlinesByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airline]]] =
    selectAirlineByCountry[V](field, values, operator).asNel(Some(field), Some(values)).execute

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
