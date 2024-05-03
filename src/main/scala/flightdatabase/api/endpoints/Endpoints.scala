package flightdatabase.api.endpoints

import cats.Monad
import cats.syntax.flatMap._
import flightdatabase.domain._
import flightdatabase.utils.implicits.enrichString
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.RequestDslBinCompat
import org.http4s.server.Router

abstract class Endpoints[F[_]: Monad](prefix: String) extends Http4sDsl[F] {

  implicit final val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl[F]

  lazy val routes: HttpRoutes[F] = Router(prefix -> endpoints)
  def endpoints: HttpRoutes[F]

  // Support matcher objects
  object OnlyNamesFlagMatcher extends FlagQueryParamMatcher("only-names")
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")
  object FieldMatcherIdDefault extends QueryParamDecoderMatcherWithDefault[String]("field", "id")

  // Support functions
  @deprecated("Use fieldTypeMap directly instead", "0.1.0")
  final protected def withFieldValidation[A: TableBase](
    field: String
  )(block: => F[Response[F]]): F[Response[F]] = {
    val allowedFields = implicitly[TableBase[A]].fieldTypeMap.keySet
    if (allowedFields(field)) block else BadRequest(InvalidField(field).error)
  }

  // FixMe: This doesn't work as expected
  def processRequestByField[T: TableBase, O](field: String, value: String)(
    algebra: (String, Any) => F[ApiResult[O]]
  )(implicit enc: EntityEncoder[F, O]): F[Response[F]] =
    implicitly[TableBase[T]].fieldTypeMap.get(field) match {
      case Some(StringType)     => algebra(field, value).flatMap(_.toResponse)
      case Some(IntType)        => value.asInt.toResponse(algebra(field, _))
      case Some(LongType)       => value.asLong.toResponse(algebra(field, _))
      case Some(BooleanType)    => value.asBoolean.toResponse(algebra(field, _))
      case Some(BigDecimalType) => value.asBigDecimal.toResponse(algebra(field, _))
      case None                 => BadRequest(InvalidField(field).error)
    }

  final protected def processRequest[IN, OUT](
    req: Request[F],
    apiError: ApiError = EntryInvalidFormat
  )(f: IN => F[ApiResult[OUT]])(
    implicit dec: EntityDecoder[F, IN],
    enc: EntityEncoder[F, OUT]
  ): F[ApiResult[OUT]] =
    req
      .attemptAs[IN]
      .foldF[ApiResult[OUT]](
        _ => apiError.elevate[F, OUT],
        f
      )

}
