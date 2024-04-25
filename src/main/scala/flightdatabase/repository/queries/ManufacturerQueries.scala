package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits.toSqlInterpolator
import flightdatabase.domain.TableBase
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerCreate

private[repository] object ManufacturerQueries {

  def manufacturerExists(id: Long): Query0[Boolean] = idExistsQuery[Manufacturer](id)

  def selectAllManufacturers: Query0[Manufacturer] = selectAll.query[Manufacturer]

  def selectManufacturersBy[V: Put](field: String, value: V): Query0[Manufacturer] =
    (selectAll ++ whereFragment(s"manufacturer.$field", value)).query[Manufacturer]

  def selectManufacturersByCity[ET: TableBase, EV: Put](
    externalField: String,
    externalValue: EV
  ): Query0[Manufacturer] = {
    selectAll ++ innerJoinWhereFragment[Manufacturer, ET, EV](
      externalField,
      externalValue,
      Some("city_based_in")
    )
  }.query[Manufacturer]

  def insertManufacturer(model: ManufacturerCreate): Update0 =
    sql"""
         | INSERT INTO manufacturer (name, city_based_in) 
         | VALUES (${model.name}, ${model.cityBasedIn})
       """.stripMargin.update

  def modifyManufacturer(model: Manufacturer): Update0 =
    sql"""
         | UPDATE manufacturer
         | SET
         |  name = ${model.name},
         |  city_based_in = ${model.cityBasedIn}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteManufacturer(id: Long): Update0 = deleteWhereId[Manufacturer](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |   manufacturer.id, manufacturer.name, manufacturer.city_based_in
        | FROM manufacturer
      """.stripMargin
}
