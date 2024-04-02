package flightdatabase.domain.manufacturer

import flightdatabase.domain.ApiResult

trait ManufacturerAlgebra[F[_]] {
  def getManufacturers: F[ApiResult[List[ManufacturerModel]]]
  def getManufacturerOnlyNames: F[ApiResult[List[String]]]
  def getManufacturerById(id: Long): F[ApiResult[ManufacturerModel]]
  def createManufacturer(manufacturer: ManufacturerModel): F[ApiResult[Long]]
  def updateManufacturer(manufacturer: ManufacturerModel): F[ApiResult[ManufacturerModel]]
  def deleteManufacturer(id: Long): F[ApiResult[ManufacturerModel]]
}
