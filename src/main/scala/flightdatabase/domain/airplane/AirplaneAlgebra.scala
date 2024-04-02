package flightdatabase.domain.airplane

trait AirplaneAlgebra[F[_]] {
  def getAirplanes(manufacturer: Option[String]): F[List[AirplaneModel]]
  def getAirplanesOnlyNames(manufacturer: Option[String]): F[List[String]]
  def getAirplane(id: Long): F[Option[AirplaneModel]]
  def createAirplane(airplane: AirplaneModel): F[Long]
  def updateAirplane(airplane: AirplaneModel): F[Option[AirplaneModel]]
  def deleteAirplane(id: Long): F[Option[AirplaneModel]]
}
