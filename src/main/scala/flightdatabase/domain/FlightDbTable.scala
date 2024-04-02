package flightdatabase.domain

object FlightDbTable extends Enumeration {
    type Table = Value
    val AIRPLANE: Value = Value("airplane")
    val AIRPORT: Value = Value("airport")
    val CITY: Value = Value("city")
    val COUNTRY: Value = Value("country")
    val CURRENCY: Value = Value("currency")
    val FLEET: Value = Value("fleet")
    val FLEET_AIRPLANE: Value = Value("fleet_airplane")
    val FLEET_ROUTE: Value = Value("fleet_route")
    val LANGUAGE: Value = Value("language")
    val MANUFACTURER: Value = Value("manufacturer")
  }
