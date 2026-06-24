package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.ApiError
import flightdatabase.ApiOutput
import flightdatabase.ApiResult
import flightdatabase.EntryNotFound
import flightdatabase.FieldType
import flightdatabase.Got
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airline.Airline
import flightdatabase.airline_airplane._
import flightdatabase.airplane.Airplane
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirlineAirplaneRepository.PartiallyAppliedGetAllAirlineAirplanes
import flightdatabase.persistence.repository.AirlineAirplaneRepository.PartiallyAppliedGetByAirline
import flightdatabase.persistence.repository.AirlineAirplaneRepository.PartiallyAppliedGetByAirlineAirplane
import flightdatabase.persistence.repository.AirlineAirplaneRepository.PartiallyAppliedGetByAirplane
import flightdatabase.persistence.repository.queries.AirlineAirplaneQueries._
import flightdatabase.persistence.syntax.all._

class AirlineAirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirlineAirplaneAlgebra[F] {

  override def doesAirlineAirplaneExist(id: Long): F[Boolean] =
    airlineAirplaneExists(id).unique.execute

  override def getAirlineAirplanes: PartiallyAppliedGetAll[F, AirlineAirplane] =
    new PartiallyAppliedGetAllAirlineAirplanes[F]

  override def getAirlineAirplane(id: Long): F[ApiResult[AirlineAirplane]] =
    selectAirlineAirplanesBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirlineAirplane(
    airlineId: Long,
    airplaneId: Long
  ): F[ApiResult[AirlineAirplane]] = {
    val idAsNel = Nel.one(airlineId)
    EitherT(
      selectAirlineAirplanesBy("airline_id", idAsNel, Operator.Equals, ValidatedSortAndLimit.empty)
        .asNel(invalidValues = Some(idAsNel))
    ).subflatMap[ApiError, ApiOutput[AirlineAirplane]] { output =>
        val airlineAirplanes = output.value
        airlineAirplanes.find(_.airplaneId == airplaneId) match {
          case Some(airlineAirplane) => Right(Got(airlineAirplane))
          case None                  => Left(EntryNotFound((airlineId, airplaneId)))
        }
      }
      .value
      .execute
  }

  override def getAirlineAirplanesBy: PartiallyAppliedGetBy[F, AirlineAirplane] =
    new PartiallyAppliedGetByAirlineAirplane[F]

  override def getAirlineAirplanesByAirplane: PartiallyAppliedGetBy[F, AirlineAirplane] =
    new PartiallyAppliedGetByAirplane[F]

  override def getAirlineAirplanesByAirline: PartiallyAppliedGetBy[F, AirlineAirplane] =
    new PartiallyAppliedGetByAirline[F]

  override def createAirlineAirplane(airlineAirplane: AirlineAirplaneCreate): F[ApiResult[Long]] =
    insertAirlineAirplane(airlineAirplane).attemptInsert.execute

  override def updateAirlineAirplane(
    airlineAirplane: AirlineAirplane
  ): F[ApiResult[Long]] =
    modifyAirlineAirplane(airlineAirplane).attemptUpdate(airlineAirplane.id).execute

  override def partiallyUpdateAirlineAirplane(
    id: Long,
    patch: AirlineAirplanePatch
  ): F[ApiResult[AirlineAirplane]] =
    EitherT(getAirlineAirplane(id)).flatMapF { airlineAirplaneOutput =>
      val airlineAirplane = airlineAirplaneOutput.value
      val patched = AirlineAirplane.fromPatch(id, patch, airlineAirplane)
      modifyAirlineAirplane(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirlineAirplane(id: Long): F[ApiResult[Unit]] =
    deleteAirlineAirplane(id).attemptDelete(id).execute
}

object AirlineAirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirlineAirplaneRepository[F]] =
    new AirlineAirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirlineAirplaneRepository[F]] =
    Resource.pure(new AirlineAirplaneRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirlineAirplanes[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, AirlineAirplane] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[AirlineAirplane]]] =
      selectAllAirlineAirplanes(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[AirlineAirplane, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirlineAirplane[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineAirplane] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineAirplane]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineAirplanesBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByAirline[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineAirplane] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineAirplane]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineAirplanesByExternal[Airline, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByAirplane[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineAirplane] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineAirplane]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineAirplanesByExternal[Airplane, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
