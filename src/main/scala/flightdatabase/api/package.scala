package flightdatabase

import cats._
import flightdatabase.domain._
import org.http4s._
import org.http4s.dsl.Http4sDsl

package object api {

  def toResponse[F[_]: Monad, A](
    res: ApiResult[A]
  )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, A]): F[Response[F]] = {
    import dsl._

    res match {
      case Right(result: GotValue[A])                              => Ok(result.value)
      case Right(created: CreatedValue[A])                         => Created(created.value)
      case Left(value: ApiError) if ApiError.badRequests(value)    => BadRequest(value.error)
      case Left(value: ApiError) if ApiError.conflicts(value)      => Conflict(value.error)
      case Left(value: ApiError) if ApiError.notImplemented(value) => NotImplemented(value.error)
      case Left(value: EntryNotFound)                              => NotFound(value.error)
      case Left(value: UnknownError)                               => UnprocessableEntity(value.error)
      // Special case error `noItems` to return 200 OK
      case Left(value: ApiError) if ApiError.noItems(value) => Ok(value.error)
      case Left(_)                                          => InternalServerError()
    }
  }

}
