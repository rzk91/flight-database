package flightdatabase.domain.fleet

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class FleetModel(
  id: Option[Long],
  name: String,
  iso2: String,
  iso3: String,
  callSign: String,
  hubAt: String,
  countryId: String
) extends ModelBase {

  def uri: Uri = ???

  override def updateId(newId: Long): FleetModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"""INSERT INTO fleet 
         |       (name, iso2, iso3, call_sign, hub_airport_id, country_id)
         |   VALUES (
         |       $name, $iso2, $iso3, $callSign,
         |       ${selectIdStmt("airport", Some(hubAt), keyField = "iata")},
         |       ${selectIdStmt("country", Some(countryId), keyField = "iso2")}
         |   )
         | """.stripMargin
}
