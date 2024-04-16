package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneCreate
import flightdatabase.domain.airplane.AirplanePatch
import flightdatabase.domain.manufacturer.ManufacturerModel
import flightdatabase.repository.queries.AirplaneQueries._
import flightdatabase.utils.implicits._

class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirplaneAlgebra[F] {

  override def doesAirplaneExist(id: Long): F[Boolean] = airplaneExists(id).unique.execute

  override def getAirplanes: F[ApiResult[List[Airplane]]] =
    selectAllAirplanes.asList.execute

  override def getAirplanesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Airplane, String]("name").execute

  override def getAirplane(id: Long): F[ApiResult[Airplane]] =
    selectAirplanesBy("id", id).asSingle(id).execute

  override def getAirplanes[V: Put](field: String, value: V): F[ApiResult[List[Airplane]]] =
    selectAirplanesBy(field, value).asList.execute

  override def getAirplanesByManufacturer(manufacturer: String): F[ApiResult[List[Airplane]]] =
    selectAirplanesByExternal[ManufacturerModel, String]("name", manufacturer).asList.execute

  override def createAirplane(airplane: AirplaneCreate): F[ApiResult[Long]] =
    insertAirplane(airplane).attemptInsert.execute

  override def updateAirplane(airplane: Airplane): F[ApiResult[Airplane]] =
    modifyAirplane(airplane).attemptUpdate(airplane).execute

  override def partiallyUpdateAirplane(
    id: Long,
    patch: AirplanePatch
  ): F[ApiResult[Airplane]] =
    EitherT(getAirplane(id)).flatMapF { airplaneOutput =>
      val airplane = airplaneOutput.value
      updateAirplane(Airplane.fromPatch(id, patch, airplane))
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
