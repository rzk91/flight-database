package flightdatabase.domain.airport

trait AirportAlgebra[F[_]] {
  def getAirports: F[List[AirportModel]]
  def getAirportsOnlyNames: F[List[String]]
  def getAirportById(id: Long): F[Option[AirportModel]]
  def getAirportByIata(iata: String): F[Option[AirportModel]]
  def getAirportByIcao(icao: String): F[Option[AirportModel]]
  def getAirportByCity(city: String): F[List[AirportModel]]
  def getAirportByCountry(country: String): F[List[AirportModel]]
  def createAirport(airport: AirportModel): F[Long]
  def updateAirport(airport: AirportModel): F[Option[AirportModel]]
  def deleteAirport(id: Long): F[Option[AirportModel]]
}
