package flightdatabase.domain

// API errors
sealed trait ApiError { def error: String }

case object EntryAlreadyExists extends ApiError {
  override def error: String = "Error: Entry or a unique field therein already exists"
}

case object EntryCheckFailed extends ApiError {

  override def error: String =
    "Error: Entry contains fields that cannot be blank or non-positive"
}

case object EntryNullCheckFailed extends ApiError {
  override def error: String = "Error: Entry contains fields that cannot be null"
}

case object EntryInvalidFormat extends ApiError {
  override def error: String = "Error: Entry has invalid format"
}

case object EntryListEmpty extends ApiError {
  override def error: String = "No items found"
}

case class EntryNotFound(entry: String) extends ApiError {
  override def error: String = s"Error: Entry $entry not found"
}

case object FeatureNotImplemented extends ApiError {
  override def error: String = "Error: Feature still under development..."
}

case class UnknownError(error: String) extends ApiError
