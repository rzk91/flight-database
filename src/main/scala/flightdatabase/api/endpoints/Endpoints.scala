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

  implicit final val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl.apply[F]

  lazy val routes: HttpRoutes[F] = Router(prefix -> endpoints)
  def endpoints: HttpRoutes[F]

  // Support matcher objects
  object OnlyNamesFlagMatcher extends FlagQueryParamMatcher("only-names")
  object FullOutputFlagMatcher extends FlagQueryParamMatcher("full-output")
  object FieldMatcherIdDefault extends QueryParamDecoderMatcherWithDefault[String]("field", "id")

  // Support functions
  final protected def idToResponse[A](id: String, apiError: ApiError = EntryInvalidFormat)(
    f: Long => F[ApiResult[A]]
  )(implicit enc: EntityEncoder[F, A]): F[Response[F]] =
    id.asLong.fold(BadRequest(apiError.error))(f(_).flatMap(toResponse(_)))

  final protected def idsToResponse[A](
    id1: String,
    id2: String,
    apiError: ApiError = EntryHasInvalidForeignKey
  )(f: (Long, Long) => F[ApiResult[A]])(implicit enc: EntityEncoder[F, A]): F[Response[F]] =
    (id1.asLong, id2.asLong) match {
      case (Some(id1), Some(id2)) => f(id1, id2).flatMap(toResponse(_))
      case _                      => BadRequest(apiError.error)
    }

  final protected def toResponse[A](
    res: ApiResult[A]
  )(implicit enc: EntityEncoder[F, A]): F[Response[F]] =
    res match {
      case Right(result: Got[A])                   => Ok(result.value)
      case Right(created: Created[A])              => Created(created.value)
      case Right(updated: Updated[A])              => Ok(updated.value)
      case Right(Deleted)                          => NoContent()
      case Left(value @ EntryListEmpty)            => Ok(value.error) // Not really an error
      case Left(value @ EntryCheckFailed)          => BadRequest(value.error)
      case Left(value @ EntryNullCheckFailed)      => BadRequest(value.error)
      case Left(value @ EntryInvalidFormat)        => BadRequest(value.error)
      case Left(value @ EntryHasInvalidForeignKey) => BadRequest(value.error)
      case Left(value: InconsistentIds)            => BadRequest(value.error)
      case Left(value: InvalidTimezone)            => BadRequest(value.error)
      case Left(value: InvalidField)               => BadRequest(value.error)
      case Left(value @ EntryAlreadyExists)        => Conflict(value.error)
      case Left(value @ FeatureNotImplemented)     => NotImplemented(value.error)
      case Left(value: EntryNotFound[_])           => NotFound(value.error)
      case Left(value: SqlError)                   => UnprocessableEntity(value.error)
      case Left(value: UnknownDbError)             => InternalServerError(value.error)
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
