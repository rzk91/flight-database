package flightdatabase.domain.fleet_route

import flightdatabase.domain._
import flightdatabase.domain.FlightDbTable.FLEET_ROUTE
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetRouteModel(
  id: Option[Long],
  fleetId: String,
  airplaneId: String,
  @JsonKey("route_number") route: String,
  start: String,
  destination: String
)

object FleetRouteModel {
  implicit val fleetRouteModelTable: TableBase[FleetRouteModel] = TableBase.instance(FLEET_ROUTE)
}
//    sql"""INSERT INTO fleet_route
//         |    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
//         |  VALUES (
//         |    (SELECT id FROM fleet_airplane
//         |      WHERE fleet_id = ${selectIdStmt("fleet", Some(fleetId))}
//         |      AND airplane_id = ${selectIdStmt("airplane", Some(airplaneId))}),
//         |      $route,
//         |      ${selectIdStmt("airport", Some(start), keyField = "iata")},
//         |      ${selectIdStmt("airport", Some(destination), keyField = "iata")}
//         |  );
//         |""".stripMargin
