package flightdatabase.model.objects

import doobie._
import doobie.implicits._
import flightdatabase.model._
import io.circe.generic.extras._
import org.http4s.Uri

@ConfiguredJsonCodec final case class FleetAirplane(
  id: Option[Long],
  fleetId: String,
  airplaneId: String
) extends FlightDbBase {

  def uri: Uri = ???

  override def sqlInsert: Fragment =
    sql"""INSERT INTO fleet_airplane
        |  	(fleet_id, airplane_id)
	    |	VALUES (
        |  	${selectIdStmt("fleet", Some(fleetId))},
        |  	${selectIdStmt("airplane", Some(airplaneId))}
	    |	)
        |""".stripMargin
}
