package flightdatabase.repository.queries

import doobie.Query0
import doobie.Update0
import doobie.implicits.toSqlInterpolator
import flightdatabase.domain.manufacturer.ManufacturerModel

private[repository] object ManufacturerQueries {

  def selectAllManufacturers: Query0[ManufacturerModel] = selectAllQuery[ManufacturerModel]

  def insertManufacturer(model: ManufacturerModel): Update0 =
    sql"""
         | INSERT INTO manufacturer (name, city_based_in) 
         | VALUES (${model.name}, ${model.basedIn})
       """.stripMargin.update

  def deleteManufacturer(id: Int): Update0 = deleteWhereId[ManufacturerModel](id)
}
