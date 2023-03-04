package flightdatabase

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityCodec._
import flightdatabase.model._
import org.http4s.headers.Location
import flightdatabase.model.objects._

package object api {

  implicit class MoreConnectionIOOps[A](private val stmt: ConnectionIO[A]) extends AnyVal {

    def execute(implicit xa: Resource[IO, HikariTransactor[IO]]): IO[A] = xa.use(stmt.transact(_))
  }

  // Convert ApiResult to IO[Response]
  // FixMe: There has to be a better way of doing this! Too much duplication...
  def toResponse[O](res: ApiResult[O]): IO[Response[IO]] = res match {
    case Right(result: GotStringList)                              => Ok(result.value)
    case Right(result: GotLanguageList)                            => Ok(result.value)
    case Right(result: GotAirplaneList)                            => Ok(result.value)
    case Right(created: CreatedAirplane)                           => Created(created.value)
    case Right(created: CreatedAirport)                            => Created(created.value)
    case Right(created: CreatedCity)                               => Created(created.value)
    case Right(created: CreatedCountry)                            => Created(created.value)
    case Right(created: CreatedCurrency)                           => Created(created.value)
    case Right(created: CreatedFleet)                              => Created(created.value)
    case Right(created: CreatedManufacturer)                       => Created(created.value)
    case Right(created: CreatedLanguage)                           => Created(created.value)
    case Right(created: CreatedFleetAirplane)                      => Created(created.value)
    case Right(created: CreatedFleetRoute)                         => Created(created.value)
    case Left(value: ApiError) if ApiError.badRequestErrors(value) => BadRequest(value.error)
    case Left(value: ApiError) if ApiError.conflictErrors(value)   => Conflict(value.error)
    case Left(value: ApiError) if ApiError.notFoundErrors(value)   => NotFound(value.error)
    case Left(value: ApiError) if ApiError.otherErrors(value)      => UnprocessableEntity(value.error)
    case Left(_)                                                   => InternalServerError()
  }

}
