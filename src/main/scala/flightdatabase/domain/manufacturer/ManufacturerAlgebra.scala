package flightdatabase.domain.manufacturer

import doobie.Put
import flightdatabase.domain.ApiResult

trait ManufacturerAlgebra[F[_]] {
  def doesManufacturerExist(id: Long): F[Boolean]
  def getManufacturers: F[ApiResult[List[Manufacturer]]]
  def getManufacturersOnlyNames: F[ApiResult[List[String]]]
  def getManufacturer(id: Long): F[ApiResult[Manufacturer]]
  def getManufacturers[V: Put](field: String, value: V): F[ApiResult[List[Manufacturer]]]
  def getManufacturersByCity(city: String): F[ApiResult[List[Manufacturer]]]
  def getManufacturersByCountry(country: String): F[ApiResult[List[Manufacturer]]]
  def createManufacturer(manufacturer: ManufacturerCreate): F[ApiResult[Long]]
  def updateManufacturer(manufacturer: Manufacturer): F[ApiResult[Long]]
  def partiallyUpdateManufacturer(id: Long, patch: ManufacturerPatch): F[ApiResult[Manufacturer]]
  def removeManufacturer(id: Long): F[ApiResult[Unit]]
}
