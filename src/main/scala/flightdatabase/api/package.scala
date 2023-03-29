package flightdatabase

import cats._
import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import flightdatabase.model._
import org.http4s._
import org.http4s.dsl.Http4sDsl

package object api {

  type ApiResult[O] = Either[ApiError, ApiOutput[O]]

  implicit class MoreConnectionIOOps[A](private val stmt: ConnectionIO[A]) extends AnyVal {

    def execute[F[_]: Async](implicit xa: Resource[F, HikariTransactor[F]]): F[A] =
      xa.use(stmt.transact(_))
  }

  def toResponse[F[_]: Monad, A](
    res: ApiResult[A]
  )(implicit dsl: Http4sDsl[F], enc: EntityEncoder[F, A]): F[Response[F]] = {
    import dsl._

    res match {
      case Right(result: GotValue[A])                                => Ok(result.value)
      case Right(created: CreatedValue[A])                           => Created(created.value)
      case Left(value: ApiError) if ApiError.badRequestErrors(value) => BadRequest(value.error)
      case Left(value: ApiError) if ApiError.conflictErrors(value)   => Conflict(value.error)
      case Left(value: ApiError) if ApiError.notFoundErrors(value)   => NotFound(value.error)
      case Left(value: ApiError) if ApiError.otherErrors(value)      => UnprocessableEntity(value.error)
      case Left(_)                                                   => InternalServerError()
    }
  }

}
