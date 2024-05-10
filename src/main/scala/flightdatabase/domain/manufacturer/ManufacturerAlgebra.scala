package flightdatabase.domain.manufacturer

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait ManufacturerAlgebra[F[_]] {
  def doesManufacturerExist(id: Long): F[Boolean]
  def getManufacturers: F[ApiResult[List[Manufacturer]]]
  def getManufacturersOnlyNames: F[ApiResult[List[String]]]
  def getManufacturer(id: Long): F[ApiResult[Manufacturer]]

  def getManufacturersBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]]

  def getManufacturersByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]]

  def getManufacturersByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]]

  def createManufacturer(manufacturer: ManufacturerCreate): F[ApiResult[Long]]
  def updateManufacturer(manufacturer: Manufacturer): F[ApiResult[Long]]
  def partiallyUpdateManufacturer(id: Long, patch: ManufacturerPatch): F[ApiResult[Manufacturer]]
  def removeManufacturer(id: Long): F[ApiResult[Unit]]
}
