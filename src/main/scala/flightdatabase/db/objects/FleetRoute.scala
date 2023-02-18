package flightdatabase.db.objects

import io.circe.generic.extras._
import flightdatabase.db.objects.DbObject._

@ConfiguredJsonCodec final case class FleetRoute(
  fleetId: String,
  airplaneId: String,
  @JsonKey("route_number") route: String,
  start: String,
  destination: String
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO fleet_route
        |    (fleet_airplane_id, route_number, start_airport_id, destination_airport_id)
        |  VALUES (
        |    (SELECT id FROM fleet_airplane 
        |      WHERE fleet_id = ${selectIdStmt("fleet", Some(fleetId))}
        |      AND airplane_id = ${selectIdStmt("airplane", Some(airplaneId))}),
        |      '$route',
        |      ${selectIdStmt("airport", Some(start), keyField = "iata")},
        |      ${selectIdStmt("airport", Some(destination), keyField = "iata")}
        |  );
        |""".stripMargin
}
