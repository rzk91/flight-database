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
import flightdatabase.airplane.Airplane
import flightdatabase.airplane.AirplaneAlgebra
import flightdatabase.airplane.AirplaneCreate
import flightdatabase.airplane.AirplanePatch
import flightdatabase.manufacturer.Manufacturer
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirplaneRepository.PartiallyAppliedGetAllAirplanes
import flightdatabase.persistence.repository.AirplaneRepository.PartiallyAppliedGetByAirplane
import flightdatabase.persistence.repository.AirplaneRepository.PartiallyAppliedGetByManufacturer
import flightdatabase.persistence.repository.queries.AirplaneQueries._
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirplaneAlgebra[F] {

  override def doesAirplaneExist(id: Long): F[Boolean] = airplaneExists(id).unique.execute

  override def getAirplanes: PartiallyAppliedGetAll[F, Airplane] =
    new PartiallyAppliedGetAllAirplanes[F]

  override def getAirplane(id: Long): F[ApiResult[Airplane]] =
    selectAirplanesBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirplanesBy: PartiallyAppliedGetBy[F, Airplane] =
    new PartiallyAppliedGetByAirplane[F]

  override def getAirplanesByManufacturer: PartiallyAppliedGetBy[F, Airplane] =
    new PartiallyAppliedGetByManufacturer[F]

  override def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]] =
    insertAirplane(airplane).attemptInsert.execute

  override def updateAirplane(airplane: Airplane): F[ApiResult[Long]] =
    modifyAirplane(airplane).attemptUpdate(airplane.id).execute

  override def partiallyUpdateAirplane(
    id: Long,
    patch: AirplanePatch
  ): F[ApiResult[Airplane]] =
    EitherT(getAirplane(id)).flatMapF { airplaneOutput =>
      val airplane = airplaneOutput.value
      val patched = Airplane.fromPatch(id, patch, airplane)
      modifyAirplane(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirplane(id: Long): F[ApiResult[Unit]] =
    deleteAirplane(id).attemptDelete(id).execute
}

object AirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirplaneRepository[F]] =
    new AirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirplaneRepository[F]] =
    Resource.pure(new AirplaneRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirplanes[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Airplane] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Airplane]]] =
      selectAllAirplanes(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Airplane, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirplane[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airplane] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airplane]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirplanesBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByManufacturer[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airplane] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airplane]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirplanesByExternal[Manufacturer, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
