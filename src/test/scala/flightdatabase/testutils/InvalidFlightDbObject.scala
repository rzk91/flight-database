package flightdatabase.testutils

import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec

@ConfiguredJsonCodec case class InvalidFlightDbObject(invalid: String)

object InvalidFlightDbObject {
  val instance: InvalidFlightDbObject = InvalidFlightDbObject("invalid")
}
