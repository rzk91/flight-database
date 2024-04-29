package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.TableBase
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportCreate

private[repository] object AirportQueries {

  def airportExists(id: Long): Query0[Boolean] = idExistsQuery[Airport](id)

  def selectAllAirports: Query0[Airport] = selectAll.query[Airport]

  def selectAirportsBy[V: Put](field: String, value: V): Query0[Airport] =
    (selectAll ++ whereFragment(s"airport.$field", value)).query[Airport]

  def selectAllAirportsByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[Airport] = {
    selectAll ++ innerJoinWhereFragment[Airport, ET, EV](
      externalField,
      externalValue
    )
  }.query[Airport]

  def insertAirport(model: AirportCreate): Update0 =
    sql"""INSERT INTO airport
         |       (name, icao, iata, city_id,
         |       number_of_runways, number_of_terminals, capacity,
         |       international, junction)
         |   VALUES (
         |       ${model.name},
         |       ${model.icao.toUpperCase},
         |       ${model.iata.toUpperCase},
         |       ${model.cityId},
         |       ${model.numRunways},
         |       ${model.numTerminals},
         |       ${model.capacity},
         |       ${model.international},
         |       ${model.junction}
         |   )
         | """.stripMargin.update

  def modifyAirport(model: Airport): Update0 =
    sql"""
         | UPDATE airport
         | SET
         |  name = ${model.name},
         |  icao = ${model.icao.toUpperCase},
         |  iata = ${model.iata.toUpperCase},
         |  city_id = ${model.cityId},
         |  number_of_runways = ${model.numRunways},
         |  number_of_terminals = ${model.numTerminals},
         |  capacity = ${model.capacity},
         |  international = ${model.international},
         |  junction = ${model.junction}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirport(id: Long): Update0 = deleteWhereId[Airport](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  airport.id, airport.name, airport.icao, airport.iata, airport.city_id,
        |  airport.number_of_runways, airport.number_of_terminals, airport.capacity,
        |  airport.international, airport.junction
        |FROM airport
      """.stripMargin

}
