package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airline.Airline
import flightdatabase.airline.AirlineCreate
import flightdatabase.country.Country
import flightdatabase.persistence.syntax.sortandlimit._
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._

private[repository] object AirlineQueries {

  def airlineExists(id: Long): Query0[Boolean] = idExistsQuery[Airline](id)

  def selectAllAirlines(sortAndLimit: ValidatedSortAndLimit): Query0[Airline] =
    (selectAll ++ sortAndLimit.fragment).query[Airline]

  def selectAirlineBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airline] =
    (selectAll ++ whereFragment(s"airline.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Airline]

  def selectAirlineByCountry[EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airline] = {
    selectAll ++ innerJoinWhereFragment[Airline, Country, EV](
      externalField,
      externalValues,
      operator
    ) ++ sortAndLimit.fragment
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
