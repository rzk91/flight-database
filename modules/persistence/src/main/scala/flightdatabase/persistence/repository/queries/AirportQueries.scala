package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import flightdatabase.Operator
import flightdatabase.TableBase
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airport.Airport
import flightdatabase.airport.AirportCreate
import flightdatabase.persistence.syntax.sortandlimit._
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._

private[repository] object AirportQueries {

  def airportExists(id: Long): Query0[Boolean] = idExistsQuery[Airport](id)

  def selectAllAirports(sortAndLimit: ValidatedSortAndLimit): Query0[Airport] =
    (selectAll ++ sortAndLimit.fragment).query[Airport]

  def selectAirportsBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airport] =
    (selectAll ++ whereFragment(s"airport.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Airport]

  def selectAllAirportsByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airport] = {
    selectAll ++ innerJoinWhereFragment[Airport, ET, EV](
      externalField,
      externalValues,
      operator
    ) ++ sortAndLimit.fragment
  }.query[Airport]

  def insertAirport(model: AirportCreate): Update0 =
    sql"""INSERT INTO airport
         |       (name, icao, iata, city_id,
         |       number_of_runways, number_of_terminals, capacity,
         |       international, junction,
         |       latitude, longitude, typical_taxi_out_minutes, typical_taxi_in_minutes)
         |   VALUES (
         |       ${model.name},
         |       ${model.icao.toUpperCase},
         |       ${model.iata.toUpperCase},
         |       ${model.cityId},
         |       ${model.numRunways},
         |       ${model.numTerminals},
         |       ${model.capacity},
         |       ${model.international},
         |       ${model.junction},
         |       ${model.latitude},
         |       ${model.longitude},
         |       ${model.typicalTaxiOutMinutes},
         |       ${model.typicalTaxiInMinutes}
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
         |  junction = ${model.junction},
         |  latitude = ${model.latitude},
         |  longitude = ${model.longitude},
         |  typical_taxi_out_minutes = ${model.typicalTaxiOutMinutes},
         |  typical_taxi_in_minutes = ${model.typicalTaxiInMinutes}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirport(id: Long): Update0 = deleteWhereId[Airport](id)

  private def selectAll: Fragment =
    fr"""
        |SELECT
        |  airport.id, airport.name, airport.icao, airport.iata, airport.city_id,
        |  airport.number_of_runways, airport.number_of_terminals, airport.capacity,
        |  airport.international, airport.junction,
        |  airport.latitude, airport.longitude,
        |  airport.typical_taxi_out_minutes, airport.typical_taxi_in_minutes
        |FROM airport
      """.stripMargin

}
