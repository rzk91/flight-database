package flightdatabase.domain

object FlightDbTable extends Enumeration {
  type Table = Value
  val AIRPLANE: Value = Value("airplane")
  val AIRPORT: Value = Value("airport")
  val CITY: Value = Value("city")
  val COUNTRY: Value = Value("country")
  val CURRENCY: Value = Value("currency")
  val AIRLINE: Value = Value("airline")
  val AIRLINE_AIRPLANE: Value = Value("airline_airplane")
  val AIRLINE_CITY: Value = Value("airline_city")
  val AIRLINE_ROUTE: Value = Value("airline_route")
  val LANGUAGE: Value = Value("language")
  val MANUFACTURER: Value = Value("manufacturer")
}
