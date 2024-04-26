package flightdatabase.domain

import cats.Applicative
import cats.syntax.either._

// API errors
sealed trait ApiError {
  def error: String
  def asResult[A]: ApiResult[A] = this.asLeft[ApiOutput[A]]
  def elevate[F[_]: Applicative, A]: F[ApiResult[A]] = Applicative[F].pure(asResult)
}

case object EntryAlreadyExists extends ApiError {
  override val error: String = "Error: Entry or a unique field therein already exists"
}

case object EntryCheckFailed extends ApiError {

  override val error: String =
    "Error: Entry contains fields that cannot be blank or non-positive"
}

case object EntryNullCheckFailed extends ApiError {
  override val error: String = "Error: Entry contains fields that cannot be null"
}

case object EntryInvalidFormat extends ApiError {
  override val error: String = "Error: Entry has invalid format"
}

case class InconsistentIds(id1: Long, id2: Long) extends ApiError {
  override val error: String = s"Error: Inconsistent ids $id1 and $id2"
}

case object EntryListEmpty extends ApiError {
  override val error: String = "No entries found"
}

case object EntryHasInvalidForeignKey extends ApiError {
  override val error: String = "Error: Entry has an invalid/non-existing foreign key"
}

case class EntryNotFound[A](entry: A) extends ApiError {
  override val error: String = s"Error: Entry $entry not found"
}

case object FeatureNotImplemented extends ApiError {
  override val error: String = "Error: Feature still under development..."
}

case class InvalidTimezone(timezone: String) extends ApiError {
  override val error: String = s"Error: Invalid timezone $timezone"
}

case class InvalidField(field: String) extends ApiError {
  override val error: String = s"Error: Invalid field $field"
}

case class UnknownDbError(error: String) extends ApiError
