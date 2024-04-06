package flightdatabase.repository.queries

import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.fleet_airplane.FleetAirplaneModel

private[repository] object FleetAirplaneQueries {

  def selectAllFleetAirplanes: Query0[FleetAirplaneModel] = selectAllQuery[FleetAirplaneModel]

  def insertFleetAirplane(model: FleetAirplaneModel): Update0 =
    sql"""INSERT INTO fleet_airplane
         |  	(fleet_id, airplane_id)
	     |	VALUES (
         |  	${model.fleetId},
         |  	${model.airplaneId}
	     |	)
         |""".stripMargin.update

  def deleteFleetAirplane(id: Int): Update0 = deleteWhereId[FleetAirplaneModel](id)
}
