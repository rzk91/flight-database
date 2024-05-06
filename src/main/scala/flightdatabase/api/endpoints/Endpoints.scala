package flightdatabase.api.endpoints

import cats.Monad
import cats.implicits.toBifunctorOps
import flightdatabase.api.Operator
import flightdatabase.api.Operator.StringOperatorOps
import flightdatabase.domain._
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
  object ValueMatcher extends QueryParamDecoderMatcher[String]("value")
  object FieldMatcherIdDefault extends QueryParamDecoderMatcherWithDefault[String]("field", "id")

  implicit val operatorQueryParamDecoder: QueryParamDecoder[Operator] =
    QueryParamDecoder[String].emap { s =>
      s.toOperator.leftMap(error => ParseFailure(error.getMessage(), ""))
    }

  object OperatorMatcherEqDefault
      extends QueryParamDecoderMatcherWithDefault[Operator]("operator", Operator.Equals)

  // Support functions
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
