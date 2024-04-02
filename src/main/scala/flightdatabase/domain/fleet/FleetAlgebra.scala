package flightdatabase.domain.fleet

trait FleetAlgebra[F[_]] {
  def getFleet(airline: Option[String]): F[List[FleetModel]]
  def getFleetOnlyNames(airline: Option[String]): F[List[String]]
  def getFleetById(id: Long): F[Option[FleetModel]]
  def createFleet(fleet: FleetModel): F[Long]
  def updateFleet(fleet: FleetModel): F[Option[FleetModel]]
  def deleteFleet(id: Long): F[Option[FleetModel]]
}
