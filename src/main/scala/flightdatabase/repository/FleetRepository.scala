package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.fleet.FleetAlgebra
import flightdatabase.domain.fleet.FleetModel
import flightdatabase.repository.queries.FleetQueries._
import flightdatabase.utils.implicits._

class FleetRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends FleetAlgebra[F] {

  override def getFleets: F[ApiResult[List[FleetModel]]] = selectAllFleets.asList.execute

  override def getFleetsOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[FleetModel, String]("name").execute

  override def getFleet(id: Long): F[ApiResult[FleetModel]] =
    featureNotImplemented[F, FleetModel]

  override def getFleetByName(name: String): F[ApiResult[FleetModel]] =
    featureNotImplemented[F, FleetModel]

  override def getFleetsByHub(hub: String): F[ApiResult[List[FleetModel]]] =
    featureNotImplemented[F, List[FleetModel]]

  override def createFleet(fleet: FleetModel): F[ApiResult[Long]] =
    insertFleet(fleet).attemptInsert.execute

  override def updateFleet(fleet: FleetModel): F[ApiResult[FleetModel]] =
    featureNotImplemented[F, FleetModel]

  override def removeFleet(id: Long): F[ApiResult[Unit]] =
    deleteFleet(id).attemptDelete(id).execute
}

object FleetRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[FleetRepository[F]] =
    new FleetRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, FleetRepository[F]] =
    Resource.pure(new FleetRepository[F])
}
