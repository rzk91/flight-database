package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.TableBase
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airplane.AirplaneCreate

private[repository] object AirplaneQueries {

  def airplaneExists(id: Long): Query0[Boolean] = idExistsQuery[Airplane](id)

  def selectAllAirplanes: Query0[Airplane] = selectAll.query[Airplane]

  def selectAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): Query0[Airplane] =
    (selectAll ++ whereFragment(s"airplane.$field", values, operator)).query[Airplane]

  def selectAirplanesByExternal[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator
  ): Query0[Airplane] = {
    selectAll ++ innerJoinWhereFragment[Airplane, ET, EV](
      externalField,
      externalValues,
      operator
    )
  }.query[Airplane]

  def insertAirplane(model: AirplaneCreate): Update0 =
    sql"""INSERT INTO airplane
         |       (name, manufacturer_id, capacity, max_range_in_km)
         |   VALUES (
         |       ${model.name},
         |       ${model.manufacturerId},
         |       ${model.capacity},
         |       ${model.maxRangeInKm}
         |   )
         | """.stripMargin.update

  def modifyAirplane(model: Airplane): Update0 =
    sql"""
         | UPDATE airplane
         | SET
         |  name = ${model.name},
         |  manufacturer_id = ${model.manufacturerId},
         |  capacity = ${model.capacity},
         |  max_range_in_km = ${model.maxRangeInKm}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteAirplane(id: Long): Update0 = deleteWhereId[Airplane](id)

  private def selectAll: Fragment =
    fr"""
         | SELECT
         |  airplane.id, airplane.name, airplane.manufacturer_id,
         |  airplane.capacity, airplane.max_range_in_km
         | FROM airplane
       """.stripMargin
}
