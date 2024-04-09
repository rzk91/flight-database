package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.fleet_route.FleetRouteAlgebra
import flightdatabase.domain.fleet_route.FleetRouteModel
import flightdatabase.repository.queries.FleetRouteQueries._
import flightdatabase.utils.implicits._

class FleetRouteRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends FleetRouteAlgebra[F] {

  override def getFleetRoutes: F[ApiResult[List[FleetRouteModel]]] =
    selectAllFleetRoutes.asList.execute

  override def getFleetRoute(id: Long): F[ApiResult[FleetRouteModel]] =
    featureNotImplemented[F, FleetRouteModel]

  override def getFleetRoutesByRouteNumber(
    routeNumber: String
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getFleetRoutesByFleetName(fleetName: String): F[ApiResult[List[FleetRouteModel]]] =
    featureNotImplemented[F, List[FleetRouteModel]]

  override def getInboundFleetRoutesByAirportId(
    airportId: Long
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getOutboundFleetRoutesByAirportId(
    airportId: Long
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getFleetRoutesByAirplaneId(airplaneId: Long): F[ApiResult[List[FleetRouteModel]]] =
    featureNotImplemented[F, List[FleetRouteModel]]

  override def createFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[Long]] =
    insertFleetRoute(fleetRoute).attemptInsert.execute

  override def updateFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[FleetRouteModel]] =
    featureNotImplemented[F, FleetRouteModel]

  override def removeFleetRoute(id: Long): F[ApiResult[Unit]] =
    deleteFleetRoute(id).attemptDelete(id).execute
}

object FleetRouteRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[FleetRouteRepository[F]] =
    new FleetRouteRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, FleetRouteRepository[F]] =
    Resource.pure(new FleetRouteRepository[F])
}
