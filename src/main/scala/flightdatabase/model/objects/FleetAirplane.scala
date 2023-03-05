package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model.objects.FlightDbBase._
import io.circe.generic.extras._

@ConfiguredJsonCodec final case class FleetAirplane(
  id: Option[Long],
  fleetId: String,
  airplaneId: String
) extends FlightDbBase {

  def sqlInsert: Fragment =
    sql"""INSERT INTO fleet_airplane
        |  	(fleet_id, airplane_id)
	    |	VALUES (
        |  	${selectIdStmt("fleet", Some(fleetId))},
        |  	${selectIdStmt("airplane", Some(airplaneId))}
	    |	)
        |""".stripMargin
}
