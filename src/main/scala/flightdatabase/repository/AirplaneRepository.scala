package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneModel
import flightdatabase.domain.manufacturer.ManufacturerModel
import flightdatabase.repository.queries.AirplaneQueries._
import flightdatabase.utils.implicits._

class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirplaneAlgebra[F] {

  override def getAirplanes: F[ApiResult[List[AirplaneModel]]] =
    selectAllAirplanes.asList.execute

  override def getAirplanesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[AirplaneModel, String]("name").execute

  override def getAirplane(id: Long): F[ApiResult[AirplaneModel]] =
    selectAirplanesBy("id", id).asSingle(id).execute

  override def getAirplanes[V: Put](field: String, value: V): F[ApiResult[List[AirplaneModel]]] =
    selectAirplanesBy(field, value).asList.execute

  override def getAirplanesByManufacturer(manufacturer: String): F[ApiResult[List[AirplaneModel]]] =
    selectAllAirplanesByExternal[ManufacturerModel, String]("name", manufacturer).asList.execute

  override def createAirplane(airplane: AirplaneModel): F[ApiResult[Long]] =
    insertAirplane(airplane).attemptInsert.execute

  override def updateAirplane(airplane: AirplaneModel): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]

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
