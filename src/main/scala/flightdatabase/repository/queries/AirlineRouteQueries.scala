package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.TableBase
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteCreate

private[repository] object AirlineRouteQueries {

  def airlineRouteExists(id: Long): Query0[Boolean] = idExistsQuery[AirlineRoute](id)

  def selectAllAirlineRoutes: Query0[AirlineRoute] = selectAll.query[AirlineRoute]

  def selectAirlineRouteBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): Query0[AirlineRoute] =
    (selectAll ++ whereFragment(s"airline_route.$field", values, operator)).query[AirlineRoute]

  def selectAirlineRoutesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    overrideExternalIdField: Option[String] = None
  ): Query0[AirlineRoute] = {
    selectAll ++ innerJoinWhereFragment[AirlineRoute, ET, EV](
      externalField,
      externalValues,
      operator,
      overrideExternalIdField
    )
  }.query[AirlineRoute]

  def insertAirlineRoute(model: AirlineRouteCreate): Update0 =
    sql"""INSERT INTO airline_route
         |    (airline_airplane_id, route_number, start_airport_id, destination_airport_id)
         |  VALUES (
         |      ${model.airlineAirplaneId},
         |      ${model.route},
         |      ${model.start},
         |      ${model.destination}
         |  );
         |""".stripMargin.update

  def modifyAirlineRoute(model: AirlineRoute): Update0 =
    sql"""
         | UPDATE airline_route
         | SET
         |  airline_airplane_id = ${model.airlineAirplaneId},
         |  route_number = ${model.route},
         |  start_airport_id = ${model.start},
         |  destination_airport_id = ${model.destination}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirlineRoute(id: Long): Update0 = deleteWhereId[AirlineRoute](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  airline_route.id, 
        |  airline_route.airline_airplane_id, 
        |  airline_route.route_number,
        |  airline_route.start_airport_id, 
        |  airline_route.destination_airport_id
        | FROM airline_route
      """.stripMargin
}
