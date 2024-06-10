package flightdatabase.api.endpoints

import cats.Monad
import cats.data.{NonEmptyList => Nel}
import cats.syntax.flatMap._
import cats.syntax.foldable._
import flightdatabase.api.Operator
import flightdatabase.api.Operator.StringOperatorOps
import flightdatabase.domain._
import flightdatabase.domain.partial.PartiallyAppliedGetAll
import flightdatabase.domain.partial.PartiallyAppliedGetBy
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.RequestDslBinCompat
import org.http4s.server.Router

abstract class Endpoints[F[_]: Monad](prefix: String) extends Http4sDsl[F] {

  implicit final val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl[F]

  lazy val routes: HttpRoutes[F] = Router(prefix -> endpoints)
  def endpoints: HttpRoutes[F]

  // Support functions
  protected def withSortAndLimitValidation[IN: TableBase](sortAndLimit: SortAndLimit)(
    f: ValidatedSortAndLimit => F[Response[F]]
  ): F[Response[F]] =
    sortAndLimit
      .validate[IN]
      .fold(errors => BadRequest(errors.mkString_("Error: ", "; ", "")), f)

  final protected def processRequestBody[IN, OUT](
    req: Request[F]
  )(f: IN => F[ApiResult[OUT]])(implicit dec: EntityDecoder[F, IN]): F[ApiResult[OUT]] =
    req
      .attemptAs[IN]
      .foldF[ApiResult[OUT]](
        _ => EntryInvalidFormat.elevate[F, OUT],
        f
      )

  final protected type G[V, T] = (String, Nel[V], Operator) => F[ApiResult[Nel[T]]]

  final protected def processFilter[IN: TableBase, OUT](
    field: String,
    operatorStr: String,
    values: String
  )(
    stringF: G[String, OUT],
    intF: G[Int, OUT],
    longF: G[Long, OUT],
    boolF: G[Boolean, OUT],
    bigDecimalF: G[BigDecimal, OUT]
  )(implicit enc: EntityEncoder[F, Nel[OUT]]): F[Response[F]] =
    (operatorStr.toOperator, implicitly[TableBase[IN]].fieldTypeMap.get(field)) match {
      case (Right(operator), Some(StringType)) if StringType.operators(operator) =>
        values.asStringToResponse(field, operator)(stringF(field, _, operator))
      case (Right(operator), Some(IntType)) if IntType.operators(operator) =>
        values.asIntToResponse(field, operator)(intF(field, _, operator))
      case (Right(operator), Some(LongType)) if LongType.operators(operator) =>
        values.asLongToResponse(field, operator)(longF(field, _, operator))
      case (Right(operator), Some(BooleanType)) if BooleanType.operators(operator) =>
        values.asBooleanToResponse(field, operator)(boolF(field, _, operator))
      case (Right(operator), Some(BigDecimalType)) if BigDecimalType.operators(operator) =>
        values.asBigDecimalToResponse(field, operator)(bigDecimalF(field, _, operator))
      case (Left(parseError), _) => InvalidOperator(parseError).asResult[Nel[OUT]].toResponse[F]
      case (Right(operator), Some(fieldType)) =>
        WrongOperator(operator, field, fieldType).asResult[Nel[OUT]].toResponse[F]
      case (_, None) => InvalidField(field).asResult[Nel[OUT]].toResponse[F]
    }

  final protected def processFilter2[IN: TableBase, OUT](
    field: String,
    operatorStr: String,
    values: String,
    sortAndLimit: ValidatedSortAndLimit
  )(f: => PartiallyAppliedGetBy[F, OUT])(implicit enc: EntityEncoder[F, Nel[OUT]]): F[Response[F]] =
    (operatorStr.toOperator, implicitly[TableBase[IN]].fieldTypeMap.get(field)) match {
      case (Right(operator), Some(StringType)) if StringType.operators(operator) =>
        values.asStringToResponse(field, operator)(f(field, _, operator, sortAndLimit))
      case (Right(operator), Some(IntType)) if IntType.operators(operator) =>
        values.asIntToResponse(field, operator)(f(field, _, operator, sortAndLimit))
      case (Right(operator), Some(LongType)) if LongType.operators(operator) =>
        values.asLongToResponse(field, operator)(f(field, _, operator, sortAndLimit))
      case (Right(operator), Some(BooleanType)) if BooleanType.operators(operator) =>
        values.asBooleanToResponse(field, operator)(f(field, _, operator, sortAndLimit))
      case (Right(operator), Some(BigDecimalType)) if BigDecimalType.operators(operator) =>
        values.asBigDecimalToResponse(field, operator)(f(field, _, operator, sortAndLimit))
      case (Left(parseError), _) => InvalidOperator(parseError).asResult[Nel[OUT]].toResponse[F]
      case (Right(operator), Some(fieldType)) =>
        WrongOperator(operator, field, fieldType).asResult[Nel[OUT]].toResponse[F]
      case (_, None) => InvalidField(field).asResult[Nel[OUT]].toResponse[F]
    }

  final protected type R[V] = String => F[ApiResult[Nel[V]]]

  final protected def processReturnOnly[IN: TableBase](field: Option[String])(
    stringF: R[String],
    intF: R[Int],
    longF: R[Long],
    boolF: R[Boolean],
    bigDecimalF: R[BigDecimal],
    allF: => F[ApiResult[Nel[IN]]]
  )(implicit allEnc: EntityEncoder[F, Nel[IN]]): F[Response[F]] =
    field match {
      case Some(field) =>
        val table = implicitly[TableBase[IN]]
        val tableField = s"${table.asString}.$field"
        table.fieldTypeMap.get(field) match {
          case Some(StringType)     => stringF(tableField).flatMap(_.toResponse[F])
          case Some(IntType)        => intF(tableField).flatMap(_.toResponse[F])
          case Some(LongType)       => longF(tableField).flatMap(_.toResponse[F])
          case Some(BooleanType)    => boolF(tableField).flatMap(_.toResponse[F])
          case Some(BigDecimalType) => bigDecimalF(tableField).flatMap(_.toResponse[F])
          case None                 => InvalidField(field).asResult[Nel[IN]].toResponse[F]
        }
      case None => allF.flatMap(_.toResponse[F])
    }

  final protected def processReturnOnly2[IN: TableBase](
    sortAndLimit: ValidatedSortAndLimit,
    field: Option[String]
  )(
    f: => PartiallyAppliedGetAll[F, IN]
  )(implicit allEnc: EntityEncoder[F, Nel[IN]]): F[Response[F]] =
    field match {
      case Some(field) =>
        val table = implicitly[TableBase[IN]]
        val tableField = s"${table.asString}.$field"
        table.fieldTypeMap.get(field) match {
          case Some(StringType)  => f[String](sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(IntType)     => f[Int](sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(LongType)    => f[Long](sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(BooleanType) => f[Boolean](sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(BigDecimalType) =>
            f[BigDecimal](sortAndLimit, tableField).flatMap(_.toResponse[F])
          case None => InvalidField(field).asResult[Nel[IN]].toResponse[F]
        }
      case None => f(sortAndLimit).flatMap(_.toResponse[F])
    }
}
