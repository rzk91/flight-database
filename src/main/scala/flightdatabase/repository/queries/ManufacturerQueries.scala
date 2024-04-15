package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits.toSqlInterpolator
import flightdatabase.domain.manufacturer.ManufacturerModel

private[repository] object ManufacturerQueries {

  def selectAllManufacturers: Query0[ManufacturerModel] = selectAll.query[ManufacturerModel]

  def insertManufacturer(model: ManufacturerModel): Update0 =
    sql"""
         | INSERT INTO manufacturer (name, city_based_in) 
         | VALUES (${model.name}, ${model.basedIn})
       """.stripMargin.update

  def deleteManufacturer(id: Long): Update0 = deleteWhereId[ManufacturerModel](id)

  private def selectAll: Fragment =
    fr"""
        | SELECT
        |   manufacturer.id, manufacturer.name, manufacturer.city_based_in
        | FROM manufacturer
      """.stripMargin
}
