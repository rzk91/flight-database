package flightdatabase

import flightdatabase.model.objects._
import org.http4s._
import org.http4s.circe._
import io.circe._
import cats.effect._

package object model {

  type ApiResult[O] = Either[ApiError, ApiOutput[O]]

  // API Output
  sealed trait ApiOutput[O]
  // Created objects
  case class CreatedAirplane(value: Airplane) extends ApiOutput[Airplane]
  case class CreatedAirport(value: Airport) extends ApiOutput[Airport]
  case class CreatedCity(value: City) extends ApiOutput[City]
  case class CreatedCountry(value: Country) extends ApiOutput[Country]
  case class CreatedCurrency(value: Currency) extends ApiOutput[Currency]
  case class CreatedFleet(value: Fleet) extends ApiOutput[Fleet]
  case class CreatedManufacturer(value: Manufacturer) extends ApiOutput[Manufacturer]
  case class CreatedLanguage(value: Language) extends ApiOutput[Language]
  case class CreatedFleetAirplane(value: FleetAirplane) extends ApiOutput[FleetAirplane]
  case class CreatedFleetRoute(value: FleetRoute) extends ApiOutput[FleetRoute]
  // Obtained objects
  case class GotStringList(value: List[String]) extends ApiOutput[List[String]]
  case class GotLanguageList(value: List[Language]) extends ApiOutput[List[Language]]
  // TODO the rest

  // API errors
  sealed trait ApiError { def error: String = "Error: " }

  object ApiError {

    val badRequestErrors: Set[ApiError] =
      Set(EntryCheckFailed, EntryNullCheckFailed, EntryInvalidFormat)
    val conflictErrors: Set[ApiError] = Set(EntryAlreadyExists)
    val notFoundErrors: Set[ApiError] = Set(EntryNotFound)
    val otherErrors: Set[ApiError] = Set(UnknownError)
  }

  case object EntryAlreadyExists extends ApiError {
    override def error: String = s"${super.error} Entry or a unique field therein already exists"
  }

  case object EntryCheckFailed extends ApiError {

    override def error: String =
      s"${super.error} Entry contains fields that cannot be blank or non-positive"
  }

  case object EntryNullCheckFailed extends ApiError {
    override def error: String = s"${super.error} Entry contains fields that cannot be null"
  }

  case object EntryInvalidFormat extends ApiError {
    override def error: String = s"${super.error} Entry has invalid format"
  }

  case object EntryNotFound extends ApiError {
    override def error: String = s"${super.error} Entry not found"
  }

  case object UnknownError extends ApiError {
    override def error: String = s"${super.error} Something went wrong..."
  }

  // Implicit definitions
  implicit def updateAirplaneId: (Long, Airplane) => Airplane = (l, obj) => obj.copy(id = Some(l))
  implicit def updateAirportId: (Long, Airport) => Airport = (l, obj) => obj.copy(id = Some(l))
  implicit def updateCityId: (Long, City) => City = (l, obj) => obj.copy(id = Some(l))
  implicit def updateCountryId: (Long, Country) => Country = (l, obj) => obj.copy(id = Some(l))
  implicit def updateCurrencyId: (Long, Currency) => Currency = (l, obj) => obj.copy(id = Some(l))
  implicit def updateFleetId: (Long, Fleet) => Fleet = (l, obj) => obj.copy(id = Some(l))
  implicit def updateLanguageId: (Long, Language) => Language = (l, obj) => obj.copy(id = Some(l))

  implicit def updateManufacturerId: (Long, Manufacturer) => Manufacturer =
    (l, obj) => obj.copy(id = Some(l))

  implicit def updateFleetAirplaneId: (Long, FleetAirplane) => FleetAirplane =
    (l, obj) => obj.copy(id = Some(l))

  implicit def updateFleetRouteId: (Long, FleetRoute) => FleetRoute =
    (l, obj) => obj.copy(id = Some(l))
}
