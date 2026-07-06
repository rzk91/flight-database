package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airline.Airline
import flightdatabase.airline.AirlineAlgebra
import flightdatabase.airline.AirlineCreate
import flightdatabase.airline.AirlinePatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirlineRepository.PartiallyAppliedGetAllAirlines
import flightdatabase.persistence.repository.AirlineRepository.PartiallyAppliedGetByAirline
import flightdatabase.persistence.repository.AirlineRepository.PartiallyAppliedGetByCountry
import flightdatabase.persistence.repository.queries.AirlineQueries._
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class AirlineRepository[F[_]: Concurrent] private (implicit
  transactor: Transactor[F]
) extends AirlineAlgebra[F] {

  override def doesAirlineExist(id: Long): F[Boolean] = airlineExists(id).unique.execute

  override def getAirlines: PartiallyAppliedGetAll[F, Airline] =
    new PartiallyAppliedGetAllAirlines[F]

  override def getAirline(id: Long): F[ApiResult[Airline]] =
    selectAirlineBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirlinesBy: PartiallyAppliedGetBy[F, Airline] =
    new PartiallyAppliedGetByAirline[F]

  override def getAirlinesByCountry: PartiallyAppliedGetBy[F, Airline] =
    new PartiallyAppliedGetByCountry[F]

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

  def make[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): F[AirlineRepository[F]] =
    new AirlineRepository[F].pure[F]

  def resource[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): Resource[F, AirlineRepository[F]] =
    Resource.pure(new AirlineRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirlines[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Airline] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Airline]]] =
      selectAllAirlines(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Airline, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirline[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airline] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airline]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCountry[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airline] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airline]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineByCountry(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
