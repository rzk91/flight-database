package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.fleet_route.FleetRoute
import flightdatabase.domain.fleet_route.FleetRouteCreate

private[repository] object FleetRouteQueries {

  def fleetRouteExists(id: Long): Query0[Boolean] = idExistsQuery[FleetRoute](id)

  def selectAllFleetRoutes: Query0[FleetRoute] = selectAll.query[FleetRoute]

  def selectFleetRouteBy[V: Put](field: String, value: V): Query0[FleetRoute] =
    (selectAll ++ whereFragment(s"fleet_route.$field", value)).query[FleetRoute]

  def selectFleetRoutesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV,
    overrideExternalIdField: Option[String] = None
  ): Query0[FleetRoute] = {
    selectAll ++ innerJoinWhereFragment[FleetRoute, ET, EV](
      externalField,
      externalValue,
      overrideExternalIdField
    )
  }.query[FleetRoute]

  def insertFleetRoute(model: FleetRouteCreate): Update0 =
    sql"""INSERT INTO fleet_route
         |    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
         |  VALUES (
         |      ${model.fleetAirplaneId},
         |      ${model.route},
         |      ${model.start},
         |      ${model.destination}
         |  );
         |""".stripMargin.update

  def modifyFleetRoute(model: FleetRoute): Update0 =
    sql"""
         | UPDATE fleet_route
         | SET
         |  fleet_airplane_id = ${model.fleetAirplaneId},
         |  route_number = ${model.route},
         |  start_airport_id = ${model.start},
         |  destination_airport_id = ${model.destination}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteFleetRoute(id: Long): Update0 = deleteWhereId[FleetRoute](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  fleet_route.id, 
        |  fleet_route.fleet_airplane_id, 
        |  fleet_route.route_number,
        |  fleet_route.start_airport_id, 
        |  fleet_route.destination_airport_id
        | FROM fleet_route
      """.stripMargin
}
