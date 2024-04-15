package flightdatabase.domain

// API Output
sealed trait ApiOutput[O] { def value: O }

// Generic objects
case class Created[O](value: O) extends ApiOutput[O]
case class Got[O](value: O) extends ApiOutput[O]
case class Updated[O](value: O) extends ApiOutput[O]

case object Deleted extends ApiOutput[Unit] {
  override val value: Unit = ()
}
