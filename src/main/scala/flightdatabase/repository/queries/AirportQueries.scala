package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.airport.AirportModel

private[repository] object AirportQueries {

  def selectAllAirports: Query0[AirportModel] = selectAll.query[AirportModel]

  def selectAirportBy[V: Put](field: String, value: V): Query0[AirportModel] =
    (selectAll ++ whereFragment(s"airport.$field", value)).query[AirportModel]

  def selectAllAirportsByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[AirportModel] = {
    selectAll ++ innerJoinWhereFragment[AirportModel, ET, EV](
      externalField,
      externalValue
    )
  }.query[AirportModel]

  def insertAirport(model: AirportModel): Update0 =
    sql"""INSERT INTO airport
         |       (name, icao, iata, city_id,
         |       number_of_runways, number_of_terminals, capacity,
         |       international, junction)
         |   VALUES (
         |       ${model.name},
         |       ${model.icao},
         |       ${model.iata},
         |       ${model.cityId},
         |       ${model.numRunways},
         |       ${model.numTerminals},
         |       ${model.capacity},
         |       ${model.international},
         |       ${model.junction}
         |   )
         | """.stripMargin.update

  def deleteAirport(id: Long): Update0 = deleteWhereId[AirportModel](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  airport.id, airport.name, airport.icao, airport.iata, airport.city_id,
        |  airport.number_of_runways, airport.number_of_terminals, airport.capacity,
        |  airport.international, airport.junction
        |FROM airport
      """.stripMargin

}
