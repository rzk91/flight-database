package flightdatabase.domain

// API errors
sealed trait ApiError { def error: String }

object ApiError {

  val badRequestErrors: Set[ApiError] =
    Set(EntryCheckFailed, EntryNullCheckFailed, EntryInvalidFormat)
  val conflictErrors: Set[ApiError] = Set(EntryAlreadyExists)
  val notFoundErrors: Set[ApiError] = Set(EntryNotFound)
  val otherErrors: Set[ApiError] = Set(UnknownError)
}

case object EntryAlreadyExists extends ApiError {
  override def error: String = s"Error: Entry or a unique field therein already exists"
}

case object EntryCheckFailed extends ApiError {

  override def error: String =
    s"Error: Entry contains fields that cannot be blank or non-positive"
}

case object EntryNullCheckFailed extends ApiError {
  override def error: String = s"Error: Entry contains fields that cannot be null"
}

case object EntryInvalidFormat extends ApiError {
  override def error: String = s"Error: Entry has invalid format"
}

case object EntryNotFound extends ApiError {
  override def error: String = s"Error: Entry not found"
}

case object UnknownError extends ApiError {
  override def error: String = s"Error: Something went wrong..."
}
