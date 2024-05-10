package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.TableBase
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_airplane.AirlineAirplaneCreate

private[repository] object AirlineAirplaneQueries {

  def airlineAirplaneExists(id: Long): Query0[Boolean] = idExistsQuery[AirlineAirplane](id)

  def selectAllAirlineAirplanes: Query0[AirlineAirplane] = selectAll.query[AirlineAirplane]

  def selectAirlineAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): Query0[AirlineAirplane] =
    (selectAll ++ whereFragment(s"airline_airplane.$field", values, operator))
      .query[AirlineAirplane]

  def selectAirlineAirplaneByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator
  ): Query0[AirlineAirplane] = {
    selectAll ++ innerJoinWhereFragment[AirlineAirplane, ET, EV](
      externalField,
      externalValues,
      operator
    )
  }.query[AirlineAirplane]

  def insertAirlineAirplane(model: AirlineAirplaneCreate): Update0 =
    sql"""INSERT INTO airline_airplane
         |  	(airline_id, airplane_id)
	     |	VALUES (
         |  	${model.airlineId},
         |  	${model.airplaneId}
	     |	)
         |""".stripMargin.update

  def modifyAirlineAirplane(model: AirlineAirplane): Update0 =
    sql"""
         | UPDATE airline_airplane
         | SET
         |  airline_id = ${model.airlineId},
         |  airplane_id = ${model.airplaneId}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirlineAirplane(id: Long): Update0 = deleteWhereId[AirlineAirplane](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  airline_airplane.id,
        |  airline_airplane.airline_id,
        |  airline_airplane.airplane_id
        | FROM airline_airplane
      """.stripMargin
}
