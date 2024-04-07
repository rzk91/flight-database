package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.fleet_airplane.FleetAirplaneAlgebra
import flightdatabase.domain.fleet_airplane.FleetAirplaneModel
import flightdatabase.repository.queries.FleetAirplaneQueries._
import flightdatabase.utils.implicits._

class FleetAirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends FleetAirplaneAlgebra[F] {

  override def getFleetAirplanes: F[ApiResult[List[FleetAirplaneModel]]] =
    selectAllFleetAirplanes.asList.execute

  override def getFleetAirplane(id: Long): F[ApiResult[FleetAirplaneModel]] =
    featureNotImplemented[F, FleetAirplaneModel]

  override def getFleetAirplanesByAirplaneName(
    airplaneName: String
  ): F[ApiResult[List[FleetAirplaneModel]]] =
    featureNotImplemented[F, List[FleetAirplaneModel]]

  override def getFleetAirplanesByFleetName(
    fleetName: String
  ): F[ApiResult[List[FleetAirplaneModel]]] =
    featureNotImplemented[F, List[FleetAirplaneModel]]

  override def createFleetAirplane(fleetAirplane: FleetAirplaneModel): F[ApiResult[Long]] =
    insertFleetAirplane(fleetAirplane).attemptInsert.execute

  override def updateFleetAirplane(
    fleetAirplane: FleetAirplaneModel
  ): F[ApiResult[FleetAirplaneModel]] = featureNotImplemented[F, FleetAirplaneModel]

  override def removeFleetAirplane(id: Long): F[ApiResult[Unit]] =
    deleteFleetAirplane(id).attemptDelete.execute
}

object FleetAirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[FleetAirplaneRepository[F]] =
    new FleetAirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, FleetAirplaneRepository[F]] =
    Resource.pure(new FleetAirplaneRepository[F])
}
