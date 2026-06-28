package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._
import flightdatabase.Operator
import flightdatabase.TableBase
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.manufacturer.Manufacturer
import flightdatabase.manufacturer.ManufacturerCreate
import flightdatabase.persistence.syntax.sortandlimit._

private[repository] object ManufacturerQueries {

  def manufacturerExists(id: Long): Query0[Boolean] = idExistsQuery[Manufacturer](id)

  def selectAllManufacturers(sortAndLimit: ValidatedSortAndLimit): Query0[Manufacturer] =
    (selectAll ++ sortAndLimit.fragment).query[Manufacturer]

  def selectManufacturersBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Manufacturer] =
    (selectAll ++ whereFragment(s"manufacturer.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Manufacturer]

  def selectManufacturersByCity[ET: TableBase, EV: Put](
    externalField: String,
    externalValues: Nel[EV],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Manufacturer] = {
    selectAll ++ innerJoinWhereFragment[Manufacturer, ET, EV](
      externalField,
      externalValues,
      operator,
      Some("base_city_id")
    ) ++ sortAndLimit.fragment
  }.query[Manufacturer]

  def insertManufacturer(model: ManufacturerCreate): Update0 =
    sql"""
         | INSERT INTO manufacturer (name, base_city_id)
         | VALUES (${model.name}, ${model.baseCityId})
       """.stripMargin.update

  def modifyManufacturer(model: Manufacturer): Update0 =
    sql"""
         | UPDATE manufacturer
         | SET
         |  name = ${model.name},
         |  base_city_id = ${model.baseCityId}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteManufacturer(id: Long): Update0 = deleteWhereId[Manufacturer](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |   manufacturer.id, manufacturer.name, manufacturer.base_city_id
        | FROM manufacturer
      """.stripMargin
}
