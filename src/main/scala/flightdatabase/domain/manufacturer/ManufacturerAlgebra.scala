package flightdatabase.domain.manufacturer

trait ManufacturerAlgebra[F[_]] {
  def getManufacturers: F[List[ManufacturerModel]]
  def getManufacturerOnlyNames: F[List[String]]
  def getManufacturerById(id: Long): F[Option[ManufacturerModel]]
  def createManufacturer(manufacturer: ManufacturerModel): F[Long]
  def updateManufacturer(manufacturer: ManufacturerModel): F[Option[ManufacturerModel]]
  def deleteManufacturer(id: Long): F[Option[ManufacturerModel]]
}
