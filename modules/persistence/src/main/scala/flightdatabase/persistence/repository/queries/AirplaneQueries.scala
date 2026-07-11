package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import flightdatabase.Operator
import flightdatabase.TableBase
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airplane.Airplane
import flightdatabase.airplane.AirplaneCreate
import flightdatabase.persistence.syntax.sortandlimit._
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._

private[repository] object AirplaneQueries {

  def airplaneExists(id: Long): Query0[Boolean] = idExistsQuery[Airplane](id)

  def selectAllAirplanes(sortAndLimit: ValidatedSortAndLimit): Query0[Airplane] =
    (selectAll ++ sortAndLimit.fragment).query[Airplane]

  def selectAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airplane] =
    (selectAll ++ whereFragment(s"airplane.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Airplane]

  def selectAirplanesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Airplane] = {
    selectAll ++ innerJoinWhereFragment[Airplane, ET, EV](
      externalField,
      externalValues,
      operator
    ) ++ sortAndLimit.fragment
  }.query[Airplane]

  def insertAirplane(model: AirplaneCreate): Update0 =
    sql"""INSERT INTO airplane
         |       (name, manufacturer_id, capacity, max_range_in_km, cruise_speed)
         |   VALUES (
         |       ${model.name},
         |       ${model.manufacturerId},
         |       ${model.capacity},
         |       ${model.maxRangeInKm},
         |       ${model.cruiseSpeed}
         |   )
         | """.stripMargin.update

  def modifyAirplane(model: Airplane): Update0 =
    sql"""
         | UPDATE airplane
         | SET
         |  name = ${model.name},
         |  manufacturer_id = ${model.manufacturerId},
         |  capacity = ${model.capacity},
         |  max_range_in_km = ${model.maxRangeInKm},
         |  cruise_speed = ${model.cruiseSpeed}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirplane(id: Long): Update0 = deleteWhereId[Airplane](id)

  private def selectAll: Fragment =
    fr"""
         | SELECT
         |  airplane.id, airplane.name, airplane.manufacturer_id,
         |  airplane.capacity, airplane.max_range_in_km, airplane.cruise_speed
         | FROM airplane
       """.stripMargin
}
