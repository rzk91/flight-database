package flightdatabase.domain.currency

import doobie.Fragment
import doobie.implicits._
import flightdatabase.domain._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.http4s.Uri

@ConfiguredJsonCodec final case class CurrencyModel(
  id: Option[Long],
  name: String,
  iso: String,
  symbol: Option[String]
) extends ModelBase {
  def uri: Uri = ???

  override def updateId(newId: Long): CurrencyModel = copy(id = Some(newId))

  override def sqlInsert: Fragment =
    sql"INSERT INTO currency (name, iso, symbol) VALUES ($name, $iso, $symbol)"
}
