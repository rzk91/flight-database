package flightdatabase

import flightdatabase.model.objects._
import io.circe.generic.extras.Configuration

package object model {

  // Allow for snake_case to camelCase conversion automatically
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

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
