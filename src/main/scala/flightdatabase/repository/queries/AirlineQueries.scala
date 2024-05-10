package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline.AirlineCreate
import flightdatabase.domain.country.Country

private[repository] object AirlineQueries {

  def airlineExists(id: Long): Query0[Boolean] = idExistsQuery[Airline](id)

  def selectAllAirlines: Query0[Airline] = selectAll.query[Airline]

  def selectAirlineBy[V: Put](field: String, values: Nel[V], operator: Operator): Query0[Airline] =
    (selectAll ++ whereFragment(s"airline.$field", values, operator)).query[Airline]

  def selectAirlineByCountry[EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator
  ): Query0[Airline] = {
    selectAll ++ innerJoinWhereFragment[Airline, Country, EV](
      externalField,
      externalValues,
      operator
    )
  }.query[Airline]

  def insertAirline(model: AirlineCreate): Update0 =
    sql"""INSERT INTO airline
         |       (name, iata, icao, call_sign, country_id)
         |   VALUES (
         |       ${model.name}, 
         |       ${model.iata.toUpperCase},
         |       ${model.icao.toUpperCase},
         |       ${model.callSign.toUpperCase},
         |       ${model.countryId}
         |   )
         | """.stripMargin.update

  def modifyAirline(model: Airline): Update0 =
    sql"""
         | UPDATE airline
         | SET
         |  name = ${model.name},
         |  iata = ${model.iata.toUpperCase},
         |  icao = ${model.icao.toUpperCase},
         |  call_sign = ${model.callSign.toUpperCase},
         |  country_id = ${model.countryId}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirline(id: Long): Update0 = deleteWhereId[Airline](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |  airline.id, airline.name, airline.iata,
        |  airline.icao, airline.call_sign, airline.country_id
        | FROM airline
      """.stripMargin
}
