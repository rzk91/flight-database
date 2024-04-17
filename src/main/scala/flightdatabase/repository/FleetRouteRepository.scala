package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Query0
import doobie.Transactor
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.domain.fleet_route.FleetRoute
import flightdatabase.domain.fleet_route.FleetRouteAlgebra
import flightdatabase.domain.fleet_route.FleetRouteCreate
import flightdatabase.domain.fleet_route.FleetRoutePatch
import flightdatabase.repository.queries.FleetRouteQueries._
import flightdatabase.utils.implicits._

class FleetRouteRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends FleetRouteAlgebra[F] {

  override def doesFleetRouteExist(id: Long): F[Boolean] =
    fleetRouteExists(id).unique.execute

  override def getFleetRoutes: F[ApiResult[List[FleetRoute]]] =
    selectAllFleetRoutes.asList.execute

  override def getFleetRoutesOnlyRoutes: F[ApiResult[List[String]]] =
    getFieldList[FleetRoute, String]("route_number").execute

  override def getFleetRoute(id: Long): F[ApiResult[FleetRoute]] =
    selectFleetRouteBy("id", id).asSingle(id).execute

  override def getFleetRoutes[V: Put](field: String, value: V): F[ApiResult[List[FleetRoute]]] =
    selectFleetRouteBy(field, value).asList.execute

  override def getFleetRoutesByFleetId(fleetId: Long): F[ApiResult[List[FleetRoute]]] =
    selectFleetRoutesByExternal[FleetAirplane, Long]("fleet_id", fleetId).asList.execute

  override def getFleetRoutesByAirplaneId(airplaneId: Long): F[ApiResult[List[FleetRoute]]] =
    selectFleetRoutesByExternal[FleetAirplane, Long]("airplane_id", airplaneId).asList.execute

  override def getFleetRoutesByAirport[V: Put](
    field: String,
    value: V,
    inbound: Option[Boolean]
  ): F[ApiResult[List[FleetRoute]]] = {
    def q(f: String): Query0[FleetRoute] =
      selectFleetRoutesByExternal[Airport, V](field, value, Some(f))
    inbound.fold {
      val startQuery = q("start_airport_id")
      val destinationQuery = q("destination_airport_id")
      (startQuery.toFragment ++ fr"UNION" ++ destinationQuery.toFragment).query[FleetRoute]
    }(in => q(if (in) "start_airport_id" else "destination_airport_id"))
  }.asList.execute

  override def createFleetRoute(fleetRoute: FleetRouteCreate): F[ApiResult[Long]] =
    insertFleetRoute(fleetRoute).attemptInsert.execute

  override def updateFleetRoute(fleetRoute: FleetRoute): F[ApiResult[FleetRoute]] =
    modifyFleetRoute(fleetRoute).attemptUpdate(fleetRoute).execute

  override def partiallyUpdateFleetRoute(
    id: Long,
    patch: FleetRoutePatch
  ): F[ApiResult[FleetRoute]] =
    EitherT(getFleetRoute(id)).flatMapF { fleetRouteOutput =>
      val fleetRoute = fleetRouteOutput.value
      updateFleetRoute(FleetRoute.fromPatch(id, patch, fleetRoute))
    }.value

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
