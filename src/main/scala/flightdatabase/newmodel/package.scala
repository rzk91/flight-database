package flightdatabase

import org.http4s._
import model.objects.FlightDbBase
import io.circe.generic.extras._

package object newmodel {

  final case class Language(
    id: Option[Long],
    name: String,
    iso2: String,
    iso3: Option[String],
    originalName: String,
    uri: Uri
  ) extends FlightDbBase

  final case class Currency(
    id: Option[Long],
    name: String,
    iso: String,
    symbol: Option[String],
    uri: Uri
  ) extends FlightDbBase

  final case class Country(
    id: Option[Long],
    name: String,
    iso2: String,
    iso3: String,
    countryCode: Int,
    domainName: Option[String],
    mainLanguage: Language,
    secondaryLanguage: Option[Language],
    tertiaryLanguage: Option[Language],
    currency: Currency,
    nationality: String,
    uri: Uri
  ) extends FlightDbBase

  final case class City(
    id: Option[Long],
    name: String,
    country: Country,
    capital: Boolean,
    population: Int,
    latitude: Double,
    longitude: Double,
    uri: Uri
  ) extends FlightDbBase

  // TODO: Remove countryId from insert script
  final case class Airport(
    id: Option[Long],
    name: String,
    icao: String,
    iata: String,
    city: City,
    @JsonKey("number_of_runways") numRunways: Int,
    @JsonKey("number_of_terminals") numTerminals: Int,
    capacity: Int,
    international: Boolean,
    junction: Boolean,
    uri: Uri
  ) extends FlightDbBase

  // TODO: Remove countryId from insert script
  final case class Fleet(
    id: Option[Long],
    name: String,
    iso2: String,
    iso3: String,
    callSign: String,
    hubAt: City,
    uri: Uri
  ) extends FlightDbBase

  final case class Manufacturer(
    id: Option[Long],
    name: String,
    basedIn: City,
    uri: Uri
  ) extends FlightDbBase

  final case class Airplane(
    id: Option[Long],
    name: String,
    manufacturer: Manufacturer,
    capacity: Int,
    maxRangeInKm: Int,
    uri: Uri
  ) extends FlightDbBase

  final case class FleetAirplane(
    id: Option[Long],
    fleet: Fleet,
    airplane: Airplane,
    uri: Uri
  ) extends FlightDbBase

  final case class FleetRoute(
    id: Option[Long],
    fleet: Fleet,
    airplane: Airplane,
    @JsonKey("route_number") route: String,
    start: Airport,
    destination: Airport,
    uri: Uri
  ) extends FlightDbBase

}
