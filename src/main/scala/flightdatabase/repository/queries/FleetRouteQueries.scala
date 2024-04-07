package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.fleet_route.FleetRouteModel

private[repository] object FleetRouteQueries {

  def selectAllFleetRoutes: Query0[FleetRouteModel] = selectAll.query[FleetRouteModel]

  def insertFleetRoute(model: FleetRouteModel): Update0 =
    sql"""INSERT INTO fleet_route
         |    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
         |  VALUES (
         |      ${model.fleetAirplaneId},
         |      ${model.route},
         |      ${model.start},
         |      ${model.destination}
         |  );
         |""".stripMargin.update

  def deleteFleetRoute(id: Long): Update0 = deleteWhereId[FleetRouteModel](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  id, fleet_airplane_id, route_number, start_airport_id, destination_airport_id
        | FROM fleet_route
      """.stripMargin
}
