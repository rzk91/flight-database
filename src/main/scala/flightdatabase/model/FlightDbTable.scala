package flightdatabase.model

object FlightDbTable extends Enumeration {
    type Table = Value
    val AIRPLANE = Value("airplane")
    val AIRPORT = Value("airport")
    val CITY = Value("city")
    val COUNTRY = Value("country")
    val CURRENCY = Value("currency")
    val FLEET = Value("fleet")
    val FLEET_AIRPLANE = Value("fleet_airplane")
    val FLEET_ROUTE = Value("fleet_route")
    val LANGUAGE = Value("language")
    val MANUFACTURER = Value("manufacturer")
  }
