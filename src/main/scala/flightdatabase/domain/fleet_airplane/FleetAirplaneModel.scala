package flightdatabase.domain.fleet_airplane

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class FleetAirplaneModel(
  id: Option[Long],
  fleetId: String,
  airplaneId: String
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): FleetAirplaneModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"""INSERT INTO fleet_airplane
         |  	(fleet_id, airplane_id)
	     |	VALUES (
         |  	${selectIdStmt("fleet", Some(fleetId))},
         |  	${selectIdStmt("airplane", Some(airplaneId))}
	     |	)
         |""".stripMargin
}
