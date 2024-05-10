package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.TableBase
import flightdatabase.domain.airline_city.AirlineCity
import flightdatabase.domain.airline_city.AirlineCityCreate

private[repository] object AirlineCityQueries {

  def airlineCityExists(id: Long): Query0[Boolean] = idExistsQuery[AirlineCity](id)

  def selectAllAirlineCities: Query0[AirlineCity] = selectAll.query[AirlineCity]

  def selectAirlineCitiesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): Query0[AirlineCity] =
    (selectAll ++ whereFragment(s"airline_city.$field", values, operator)).query[AirlineCity]

  def selectAirlineCityByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator
  ): Query0[AirlineCity] = {
    selectAll ++ innerJoinWhereFragment[AirlineCity, ET, EV](
      externalField,
      externalValues,
      operator
    )
  }.query[AirlineCity]

  def insertAirlineCity(model: AirlineCityCreate): Update0 =
    sql"""INSERT INTO airline_city
             |  	(airline_id, city_id)
             |	VALUES (
             |  	${model.airlineId},
             |  	${model.cityId}
             |	)
             |""".stripMargin.update

  def modifyAirlineCity(model: AirlineCity): Update0 =
    sql"""
             | UPDATE airline_city
             | SET
             |  airline_id = ${model.airlineId},
             |  city_id = ${model.cityId}
             | WHERE id = ${model.id}
         """.stripMargin.update

  def deleteAirlineCity(id: Long): Update0 = deleteWhereId[AirlineCity](id)

  private def selectAll: Fragment =
    fr"""
            | SELECT
            |  airline_city.id,
            |  airline_city.airline_id,
            |  airline_city.city_id
            | FROM airline_city
        """.stripMargin
}
