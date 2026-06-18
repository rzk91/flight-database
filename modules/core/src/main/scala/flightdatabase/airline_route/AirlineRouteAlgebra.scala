package flightdatabase.airline_route

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait AirlineRouteAlgebra[F[_]] {
  def doesAirlineRouteExist(id: Long): F[Boolean]
  def getAirlineRoutes: PartiallyAppliedGetAll[F, AirlineRoute]
  def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]]
  def getAirlineRoutesBy: PartiallyAppliedGetBy[F, AirlineRoute]
  def getAirlineRoutesByAirline: PartiallyAppliedGetBy[F, AirlineRoute]
  def getAirlineRoutesByAirplane: PartiallyAppliedGetBy[F, AirlineRoute]

  // None for both inbound and outbound
  def getAirlineRoutesByAirport(inbound: Option[Boolean]): PartiallyAppliedGetBy[F, AirlineRoute]

  def createAirlineRoute(airlineRoute: AirlineRouteCreate): F[ApiResult[Long]]
  def updateAirlineRoute(airlineRoute: AirlineRoute): F[ApiResult[Long]]
  def partiallyUpdateAirlineRoute(id: Long, patch: AirlineRoutePatch): F[ApiResult[AirlineRoute]]
  def removeAirlineRoute(id: Long): F[ApiResult[Unit]]
}
