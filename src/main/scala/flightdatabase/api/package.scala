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
      case Right(result: GotValue[A])          => Ok(result.value)
      case Right(created: CreatedValue[A])     => Created(created.value)
      case Left(value @ EntryListEmpty)        => Ok(value.error) // Not really an error
      case Left(value @ EntryCheckFailed)      => BadRequest(value.error)
      case Left(value @ EntryNullCheckFailed)  => BadRequest(value.error)
      case Left(value @ EntryInvalidFormat)    => BadRequest(value.error)
      case Left(value @ EntryAlreadyExists)    => Conflict(value.error)
      case Left(value @ FeatureNotImplemented) => NotImplemented(value.error)
      case Left(value: EntryNotFound)          => NotFound(value.error)
      case Left(value: UnknownError)           => UnprocessableEntity(value.error)
    }
  }
}
