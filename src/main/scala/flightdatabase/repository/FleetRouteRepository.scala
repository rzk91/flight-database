package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.fleet_route.FleetRouteAlgebra
import flightdatabase.domain.fleet_route.FleetRouteModel
import flightdatabase.repository.queries.FleetRouteQueries._
import flightdatabase.utils.implicits._

class FleetRouteRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends FleetRouteAlgebra[F] {

  override def getFleetRoutes: F[ApiResult[List[FleetRouteModel]]] =
    selectAllFleetRoutes.asList.execute

  override def getFleetRoute(id: Int): F[ApiResult[FleetRouteModel]] =
    featureNotImplemented[F, FleetRouteModel]

  override def getFleetRoutesByRouteNumber(
    routeNumber: String
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getFleetRoutesByFleetName(fleetName: String): F[ApiResult[List[FleetRouteModel]]] =
    featureNotImplemented[F, List[FleetRouteModel]]

  override def getInboundFleetRoutesByAirportId(
    airportId: Int
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getOutboundFleetRoutesByAirportId(
    airportId: Int
  ): F[ApiResult[List[FleetRouteModel]]] = featureNotImplemented[F, List[FleetRouteModel]]

  override def getFleetRoutesByAirplaneId(airplaneId: Int): F[ApiResult[List[FleetRouteModel]]] =
    featureNotImplemented[F, List[FleetRouteModel]]

  override def createFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[Int]] =
    insertFleetRoute(fleetRoute).attemptInsert.execute

  override def updateFleetRoute(fleetRoute: FleetRouteModel): F[ApiResult[FleetRouteModel]] =
    featureNotImplemented[F, FleetRouteModel]

  override def removeFleetRoute(id: Int): F[ApiResult[Unit]] =
    deleteFleetRoute(id).attemptDelete.execute
}

object FleetRouteRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[FleetRouteRepository[F]] =
    new FleetRouteRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, FleetRouteRepository[F]] =
    Resource.pure(new FleetRouteRepository[F])
}
