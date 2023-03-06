package flightdatabase

import flightdatabase.model.objects._

package object model {

  type ApiResult[O] = Either[ApiError, ApiOutput[O]]

  // API Output
  sealed trait ApiOutput[O] { def value: O }

  // Generic objects
  case class CreatedValue[O](value: O) extends ApiOutput[O]
  case class GotValue[O](value: O) extends ApiOutput[O]

  // API errors
  sealed trait ApiError { def error: String = "Error:" }

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
