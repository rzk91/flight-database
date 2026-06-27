package flightdatabase

import cats.Applicative
import cats.data.{NonEmptyList => Nel}
import cats.syntax.either._
import enumeratum.NoSuchMember

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
  override val error: String = s"Error: Inconsistent ids '$id1' and '$id2'"
}

case object EntryListEmpty extends ApiError {
  override val error: String = "No entries found"
}

case object EntryHasInvalidForeignKey extends ApiError {
  override val error: String = "Error: Entry has an invalid/non-existing foreign key"
}

case class EntryNotFound[A](entry: A) extends ApiError {
  override val error: String = s"Error: Entry '$entry' not found"
}

case class EntryValueTooLong(field: Option[String]) extends ApiError {

  override val error: String = field match {
    case Some(value) => s"Error: Value for field '$value' exceeds maximum allowed length"
    case None        => s"Error: A value exceeds maximum allowed length"
  }
}

case class InvalidTimezone(timezone: String) extends ApiError {
  override val error: String = s"Error: Invalid timezone '$timezone'"
}

case class InvalidField(field: String) extends ApiError {
  override val error: String = s"Error: Invalid field '$field'"
}

case class InvalidOperator(parseError: NoSuchMember[Operator]) extends ApiError {
  override val error: String = s"Error: Invalid operator! ${parseError.getMessage()}"
}

case class WrongOperator(operator: Operator, field: String, fieldType: FieldType[_])
    extends ApiError {

  override val error: String =
    s"Error: Wrong operator '$operator' for field '$field' of type '$fieldType'"
}

case class InvalidValueType(value: String) extends ApiError {
  override val error: String = s"Error: Invalid type for value(s) '$value'"
}

case class SqlError(
  sqlState: String,
  relevantField: Option[String],
  relevantValues: Option[Nel[_]]
) extends ApiError {

  def addendum: String =
    (relevantField, relevantValues) match {
      case (Some(field), Some(values)) =>
        s"\nThe error occurred while processing field '$field' with value(s): '${values.toList.mkString(", ")}'."
      case (Some(field), None) =>
        s"\nThe error occurred while processing field '$field'."
      case (None, Some(values)) =>
        s"\nThe error occurred while processing value(s): '${values.toList.mkString(", ")}'."
      case _ => ""
    }

  override val error: String =
    s"Encountered PostgreSQL error: '$sqlState'." +
      addendum +
      "\nHere is a link to all SQL state error codes: " +
      "https://www.postgresql.org/docs/current/errcodes-appendix.html"
}

case class UnknownDbError(error: String) extends ApiError

case object FeatureNotImplemented extends ApiError {
  override val error: String = "Error: Feature still under development..."
}
