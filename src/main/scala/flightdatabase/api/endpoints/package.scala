package flightdatabase.api

import cats.Monad
import cats.data.Validated
import cats.data.ValidatedNel
import cats.data.{NonEmptyList => Nel}
import cats.syntax.all._
import flightdatabase.api.Operator.StringOperatorOps
import flightdatabase.domain.ResultOrder.StringOrderOps
import flightdatabase.domain._
import flightdatabase.utils.implicits.enrichOption
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.FlagQueryParamMatcher
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.dsl.impl.QueryParamDecoderMatcherWithDefault

package object endpoints {

  // All matcher objects
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")
  object ValueMatcher extends QueryParamDecoderMatcher[String]("value")
  object FieldMatcher extends QueryParamDecoderMatcher[String]("field")
  object ReturnOnlyMatcher extends OptionalQueryParamDecoderMatcher[String]("return-only")

  case class SortAndLimit(
    sortBy: Option[String],
    order: Option[String],
    limit: Option[Int],
    offset: Option[Int]
  ) {

    def validate[T: TableBase]: ValidatedNel[String, ValidatedSortAndLimit] = {
      val table = implicitly[TableBase[T]]

      val sortByValidated = Validated.condNel(
        sortBy.forall(table.fieldTypeMap.contains),
        sortBy.map(f => s"${table.asString}.$f"),
        s"Invalid entry in 'sort-by': ${sortBy.debug}"
      )

      val orderValidated = order
        .traverse(_.toOrder.leftMap(err => s"Invalid entry in 'order': ${err.getMessage()}"))
        .toValidatedNel

      val limitValidated =
        Validated.condNel(limit.forall(_ >= 0), limit, s"Invalid entry in 'limit': ${limit.debug}")

      val offsetValidated =
        Validated.condNel(
          offset.forall(_ >= 0),
          offset,
          s"Invalid entry in 'offset': ${offset.debug}"
        )

      (sortByValidated, orderValidated, limitValidated, offsetValidated).mapN(
        ValidatedSortAndLimit.apply
      )
    }
  }

  object SortAndLimit {
    private object SortByMatcher extends OptionalQueryParamDecoderMatcher[String]("sort-by")
    private object OrderMatcher extends OptionalQueryParamDecoderMatcher[String]("order")
    private object LimitMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")
    private object OffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")

    def unapply(params: Map[String, collection.Seq[String]]): Option[SortAndLimit] =
      for {
        sortBy <- SortByMatcher.unapply(params)
        order  <- OrderMatcher.unapply(params)
        limit  <- LimitMatcher.unapply(params)
        offset <- OffsetMatcher.unapply(params)
      } yield SortAndLimit(sortBy, order, limit, offset)
  }

  implicit val operatorQueryParamDecoder: QueryParamDecoder[Operator] =
    QueryParamDecoder[String].emap { s =>
      s.toOperator.leftMap(error => ParseFailure(error.getMessage(), ""))
    }

  object OperatorMatcherEqDefault
      extends QueryParamDecoderMatcherWithDefault[Operator]("operator", Operator.Equals)

  // Extension methods for various API-related functions
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
