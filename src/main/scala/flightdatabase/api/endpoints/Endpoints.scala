package flightdatabase.api.endpoints

import cats.Monad
import cats.data.{NonEmptyList => Nel}
import cats.syntax.flatMap._
import cats.syntax.foldable._
import flightdatabase.api.Operator
import flightdatabase.domain._
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
      .fold(errors => BadRequest(errors.mkString_(",\n")), f)

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
    operator: Operator,
    values: String
  )(
    stringF: G[String, OUT],
    intF: G[Int, OUT],
    longF: G[Long, OUT],
    boolF: G[Boolean, OUT],
    bigDecimalF: G[BigDecimal, OUT]
  )(implicit enc: EntityEncoder[F, Nel[OUT]]): F[Response[F]] =
    implicitly[TableBase[IN]].fieldTypeMap.get(field) match {
      case Some(StringType) if StringType.operators(operator) =>
        values.asStringToResponse(field, operator)(stringF(field, _, operator))
      case Some(IntType) if IntType.operators(operator) =>
        values.asIntToResponse(field, operator)(intF(field, _, operator))
      case Some(LongType) if LongType.operators(operator) =>
        values.asLongToResponse(field, operator)(longF(field, _, operator))
      case Some(BooleanType) if BooleanType.operators(operator) =>
        values.asBooleanToResponse(field, operator)(boolF(field, _, operator))
      case Some(BigDecimalType) if BigDecimalType.operators(operator) =>
        values.asBigDecimalToResponse(field, operator)(bigDecimalF(field, _, operator))
      case Some(_) => BadRequest(InvalidOperator(operator).error)
      case None    => BadRequest(InvalidField(field).error)
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
          case None                 => BadRequest(InvalidField(field).error)
        }
      case None => allF.flatMap(_.toResponse[F])
    }

  final protected type R2[V] = (ValidatedSortAndLimit, String) => F[ApiResult[Nel[V]]]

  final protected def processReturnOnly2[IN: TableBase](
    sortAndLimit: ValidatedSortAndLimit,
    field: Option[String]
  )(
    stringF: R2[String],
    intF: R2[Int],
    longF: R2[Long],
    boolF: R2[Boolean],
    bigDecimalF: R2[BigDecimal],
    allF: ValidatedSortAndLimit => F[ApiResult[Nel[IN]]]
  )(implicit allEnc: EntityEncoder[F, Nel[IN]]): F[Response[F]] =
    field match {
      case Some(field) =>
        val table = implicitly[TableBase[IN]]
        val tableField = s"${table.asString}.$field"
        table.fieldTypeMap.get(field) match {
          case Some(StringType)  => stringF(sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(IntType)     => intF(sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(LongType)    => longF(sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(BooleanType) => boolF(sortAndLimit, tableField).flatMap(_.toResponse[F])
          case Some(BigDecimalType) =>
            bigDecimalF(sortAndLimit, tableField).flatMap(_.toResponse[F])
          case None => BadRequest(InvalidField(field).error)
        }
      case None => allF(sortAndLimit).flatMap(_.toResponse[F])
    }
}
