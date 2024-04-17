package flightdatabase.domain

import cats.Applicative
import cats.syntax.either._

// API Output
sealed trait ApiOutput[O] {
  def value: O
  def asResult: ApiResult[O] = this.asRight[ApiError]
  def elevate[F[_]: Applicative]: F[ApiResult[O]] = Applicative[F].pure(asResult)
}

// Generic objects
case class Created[O](value: O) extends ApiOutput[O]
case class Got[O](value: O) extends ApiOutput[O]
case class Updated[O](value: O) extends ApiOutput[O]

case object Deleted extends ApiOutput[Unit] {
  override val value: Unit = ()
}
