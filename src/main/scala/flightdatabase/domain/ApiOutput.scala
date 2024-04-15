package flightdatabase.domain

// API Output
sealed trait ApiOutput[O] { def value: O }

// Generic objects
case class CreatedValue[O](value: O) extends ApiOutput[O]
case class GotValue[O](value: O) extends ApiOutput[O]
// TODO: Add case class for updated value
