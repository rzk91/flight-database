package flightdatabase.airport

import io.circe.Decoder
import io.circe.Encoder

final case class TaxiDuration(minutes: Int) extends AnyVal

object TaxiDuration {
  implicit val encoder: Encoder[TaxiDuration] = Encoder[Int].contramap(_.minutes)
  implicit val decoder: Decoder[TaxiDuration] = Decoder[Int].map(TaxiDuration.apply)
}
