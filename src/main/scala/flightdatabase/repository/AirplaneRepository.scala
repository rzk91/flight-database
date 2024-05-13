package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.airplane.AirplanePatch
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.repository.queries.AirplaneQueries._
import flightdatabase.utils.implicits._

class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirplaneAlgebra[F] {

  override def doesAirplaneExist(id: Long): F[Boolean] = airplaneExists(id).unique.execute

  override def getAirplanes: F[ApiResult[Nel[Airplane]]] =
    selectAllAirplanes.asNel().execute

  override def getAirplanesOnlyNames: F[ApiResult[Nel[String]]] =
    getFieldList[Airplane, String]("name").execute

  override def getAirplane(id: Long): F[ApiResult[Airplane]] =
    selectAirplanesBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airplane]]] =
    selectAirplanesBy(field, values, operator).asNel(Some(field), Some(values)).execute

  def getAirplanesByManufacturer[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airplane]]] =
    selectAirplanesByExternal[Manufacturer, V](field, values, operator)
      .asNel(Some(field), Some(values))
      .execute

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
}
