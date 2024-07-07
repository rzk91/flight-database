package flightdatabase.domain

import enumeratum._
import org.http4s.Uri
import org.http4s.Uri.Path

sealed abstract class FlightDbTable(override val entryName: String) extends EnumEntry {
  def prefix: String
}

object FlightDbTable extends Enum[FlightDbTable] {
  val values = findValues

  case object AIRPLANE extends FlightDbTable("airplane") { val prefix = "/airplanes" }
  case object AIRPORT extends FlightDbTable("airport") { val prefix = "/airports" }
  case object CITY extends FlightDbTable("city") { val prefix = "/cities" }
  case object COUNTRY extends FlightDbTable("country") { val prefix = "/countries" }
  case object CURRENCY extends FlightDbTable("currency") { val prefix = "/currencies" }
  case object AIRLINE extends FlightDbTable("airline") { val prefix = "/airlines" }

  case object AIRLINE_AIRPLANE extends FlightDbTable("airline_airplane") {
    val prefix = "/airline-airplanes"
  }

  case object AIRLINE_CITY extends FlightDbTable("airline_city") { val prefix = "/airline-cities" }

  case object AIRLINE_ROUTE extends FlightDbTable("airline_route") {
    val prefix = "/airline-routes"
  }
  case object LANGUAGE extends FlightDbTable("language") { val prefix = "/languages" }
  case object MANUFACTURER extends FlightDbTable("manufacturer") { val prefix = "/manufacturers" }
  case object HELLO_WORLD extends FlightDbTable("hello") { val prefix = "/hello" }

  def validEntryPoints(mainEntryPoint: Uri): IndexedSeq[Path] =
    values.map(p => mainEntryPoint.addPath(p.prefix.tail).path)
}
