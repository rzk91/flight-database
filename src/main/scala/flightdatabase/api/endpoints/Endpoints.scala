package flightdatabase.api.endpoints

import cats.Monad
import cats.data.{NonEmptyList => Nel}
import cats.syntax.bifunctor._
import cats.syntax.flatMap._
import flightdatabase.api.Operator
import flightdatabase.api.Operator.StringOperatorOps
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

  // Support matcher objects
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")
  object ValueMatcher extends QueryParamDecoderMatcher[String]("value")
  object FieldMatcher extends QueryParamDecoderMatcher[String]("field")
  object ReturnOnlyMatcher extends OptionalQueryParamDecoderMatcher[String]("return-only")

  implicit val operatorQueryParamDecoder: QueryParamDecoder[Operator] =
    QueryParamDecoder[String].emap { s =>
      s.toOperator.leftMap(error => ParseFailure(error.getMessage(), ""))
    }

  object OperatorMatcherEqDefault
      extends QueryParamDecoderMatcherWithDefault[Operator]("operator", Operator.Equals)

  // Support functions
  final protected def processRequestBody[IN, OUT](
    req: Request[F]
  )(f: IN => F[ApiResult[OUT]])(
    implicit dec: EntityDecoder[F, IN],
    enc: EntityEncoder[F, OUT]
  ): F[ApiResult[OUT]] =
    req
      .attemptAs[IN]
      .foldF[ApiResult[OUT]](
        _ => EntryInvalidFormat.elevate[F, OUT],
        f
      )

  final protected type λ1[V, T] = (String, Nel[V], Operator) => F[ApiResult[Nel[T]]]

  final protected def processFilter[IN: TableBase, OUT](
    field: String,
    operator: Operator,
    values: String
  )(
    stringF: λ1[String, OUT],
    intF: λ1[Int, OUT],
    longF: λ1[Long, OUT],
    boolF: λ1[Boolean, OUT],
    bigDecimalF: λ1[BigDecimal, OUT]
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

  final protected type λ2[V] = String => F[ApiResult[Nel[V]]]

  final protected def processReturnOnly[IN: TableBase](field: Option[String])(
    stringF: λ2[String],
    intF: λ2[Int],
    longF: λ2[Long],
    boolF: λ2[Boolean],
    bigDecimalF: λ2[BigDecimal],
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
}
