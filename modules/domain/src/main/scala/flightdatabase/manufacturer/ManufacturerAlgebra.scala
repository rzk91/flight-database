package flightdatabase.manufacturer

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait ManufacturerAlgebra[F[_]] {
  def doesManufacturerExist(id: Long): F[Boolean]
  def getManufacturers: PartiallyAppliedGetAll[F, Manufacturer]
  def getManufacturer(id: Long): F[ApiResult[Manufacturer]]
  def getManufacturersBy: PartiallyAppliedGetBy[F, Manufacturer]
  def getManufacturersByCity: PartiallyAppliedGetBy[F, Manufacturer]
  def getManufacturersByCountry: PartiallyAppliedGetBy[F, Manufacturer]
  def createManufacturer(manufacturer: ManufacturerCreate): F[ApiResult[Long]]
  def updateManufacturer(manufacturer: Manufacturer): F[ApiResult[Long]]
  def partiallyUpdateManufacturer(id: Long, patch: ManufacturerPatch): F[ApiResult[Manufacturer]]
  def removeManufacturer(id: Long): F[ApiResult[Unit]]
}
