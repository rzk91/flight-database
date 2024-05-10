package flightdatabase.api

import cats.Monad
import cats.data.{NonEmptyList => Nel}
import cats.implicits.toTraverseOps
import cats.syntax.flatMap._
import flightdatabase.domain._
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.dsl.Http4sDsl

package object endpoints {

  implicit class RichApiResult[A](private val result: ApiResult[A]) extends AnyVal {

    def toResponse[F[_]: Monad](
      implicit dsl: Http4sDsl[F],
      enc: EntityEncoder[F, A]
    ): F[Response[F]] = {
      import dsl._
      result match {
        case Right(result: Got[A])                   => Ok(result.value)
        case Right(created: Created[A])              => Created(created.value)
        case Right(updated: Updated[A])              => Ok(updated.value)
        case Right(_: Deleted.type)                  => NoContent()
        case Left(value @ EntryListEmpty)            => Ok(value.error) // Not really an error
        case Left(value @ EntryCheckFailed)          => BadRequest(value.error)
        case Left(value @ EntryNullCheckFailed)      => BadRequest(value.error)
        case Left(value @ EntryInvalidFormat)        => BadRequest(value.error)
        case Left(value @ EntryHasInvalidForeignKey) => BadRequest(value.error)
        case Left(value: InconsistentIds)            => BadRequest(value.error)
        case Left(value: InvalidTimezone)            => BadRequest(value.error)
        case Left(value: InvalidField)               => BadRequest(value.error)
        case Left(value: InvalidOperator)            => BadRequest(value.error)
        case Left(value: InvalidValueType)           => BadRequest(value.error)
        case Left(value @ EntryAlreadyExists)        => Conflict(value.error)
        case Left(value @ FeatureNotImplemented)     => NotImplemented(value.error)
        case Left(value: EntryNotFound[_])           => NotFound(value.error)
        case Left(value: SqlError)                   => UnprocessableEntity(value.error)
        case Left(value: UnknownDbError)             => InternalServerError(value.error)
      }
    }
  }

  implicit class RichOptionField[A](private val maybeField: Option[A]) extends AnyVal {

    def toResponse[F[_]: Monad, O](
      f: A => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] = {
      import dsl._
      maybeField.fold(BadRequest(EntryInvalidFormat.error))(f(_).flatMap(_.toResponse))
    }
  }

  implicit class RichOptionTuple[A, B](private val maybeFields: Option[(A, B)]) extends AnyVal {

    def toResponse[F[_]: Monad, O](
      f: (A, B) => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] = {
      import dsl._
      maybeFields.fold(BadRequest(EntryInvalidFormat.error))(f.tupled(_).flatMap(_.toResponse))
    }
  }

  implicit class ValuesOps(private val values: String) extends AnyVal {

    def asStringToResponse[F[_]: Monad, O](field: String, op: Operator)(
      f: Nel[String] => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] =
      values.splitToNel().map(_.toOption).sequence.toResponse(f)

    def asIntToResponse[F[_]: Monad, O](field: String, op: Operator)(
      f: Nel[Int] => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] =
      values.splitToNel().map(_.asInt).sequence.toResponse(f)

    def asLongToResponse[F[_]: Monad, O](field: String, op: Operator)(
      f: Nel[Long] => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] =
      values.splitToNel().map(_.asLong).sequence.toResponse(f)

    def asBooleanToResponse[F[_]: Monad, O](field: String, op: Operator)(
      f: Nel[Boolean] => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] =
      values.splitToNel().map(_.asBoolean).sequence.toResponse(f)

    def asBigDecimalToResponse[F[_]: Monad, O](field: String, op: Operator)(
      f: Nel[BigDecimal] => F[ApiResult[O]]
    )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, O]): F[Response[F]] =
      values.splitToNel().map(_.asBigDecimal).sequence.toResponse(f)
  }
}
