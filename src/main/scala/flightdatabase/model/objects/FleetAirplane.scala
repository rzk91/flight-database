package flightdatabase.model.objects

import io.circe.generic.extras._
import flightdatabase.model.objects.DbObject._

@ConfiguredJsonCodec final case class FleetAirplane(
	id: Option[Long],
  fleetId: String,
  airplaneId: String
) extends DbObject {

  def sqlInsert: String =
    s"""INSERT INTO fleet_airplane
		  |  	(fleet_id, airplane_id)
	    |	VALUES (
		  |  	${selectIdStmt("fleet", Some(fleetId))},
		  |  	${selectIdStmt("airplane", Some(airplaneId))}
	    |	);
			|""".stripMargin
}
