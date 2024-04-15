package flightdatabase.domain.manufacturer

import flightdatabase.domain.ApiResult

trait ManufacturerAlgebra[F[_]] {
  def getManufacturers: F[ApiResult[List[ManufacturerModel]]]
  def getManufacturerOnlyNames: F[ApiResult[List[String]]]
  def getManufacturer(id: Long): F[ApiResult[ManufacturerModel]]
  def getManufacturersByCity(city: String): F[ApiResult[List[ManufacturerModel]]]
  def getManufacturersByCountry(country: String): F[ApiResult[List[ManufacturerModel]]]
  def createManufacturer(manufacturer: ManufacturerModel): F[ApiResult[Long]]
  def updateManufacturer(manufacturer: ManufacturerModel): F[ApiResult[ManufacturerModel]]
  def removeManufacturer(id: Long): F[ApiResult[Unit]]
}
