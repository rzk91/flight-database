package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneModel
import flightdatabase.repository.queries.AirplaneQueries._
import flightdatabase.utils.implicits._

// TODO: Perhaps replace the resource with a simple instance of `Transactor[F]`
// Question: how does it then work with pooling then?
class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends AirplaneAlgebra[F] {

  override def getAirplanes: F[ApiResult[List[AirplaneModel]]] =
    selectAllAirplanes.asList.execute

  override def getAirplanesOnlyNames: F[ApiResult[List[String]]] =
    getNameList[AirplaneModel].execute

  override def getAirplane(id: Long): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]

  override def getAirplanesByManufacturer(manufacturer: String): F[ApiResult[List[AirplaneModel]]] =
    selectAllAirplanesByManufacturer(manufacturer).asList.execute

  override def createAirplane(airplane: AirplaneModel): F[ApiResult[Long]] =
    insertAirplane(airplane).attemptInsert.execute

  override def updateAirplane(airplane: AirplaneModel): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]

  override def removeAirplane(id: Long): F[ApiResult[Unit]] =
    deleteAirplane(id).attemptDelete.execute
}

object AirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[AirplaneRepository[F]] =
    new AirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, AirplaneRepository[F]] =
    Resource.pure(new AirplaneRepository[F])
}
