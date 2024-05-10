package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain._
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_airplane._
import flightdatabase.domain.airplane.Airplane
import flightdatabase.repository.queries.AirlineAirplaneQueries._
import flightdatabase.utils.implicits._

class AirlineAirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirlineAirplaneAlgebra[F] {

  override def doesAirlineAirplaneExist(id: Long): F[Boolean] =
    airlineAirplaneExists(id).unique.execute

  override def getAirlineAirplanes: F[ApiResult[List[AirlineAirplane]]] =
    selectAllAirlineAirplanes.asList().execute

  override def getAirlineAirplane(id: Long): F[ApiResult[AirlineAirplane]] =
    selectAirlineAirplanesBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirlineAirplane(
    airlineId: Long,
    airplaneId: Long
  ): F[ApiResult[AirlineAirplane]] = {
    val asNel = Nel.one(airlineId)
    EitherT(
      selectAirlineAirplanesBy("airline_id", asNel, Operator.Equals)
        .asList(invalidValues = Some(asNel))
    ).subflatMap[ApiError, ApiOutput[AirlineAirplane]] { output =>
        val airlineAirplanes = output.value
        airlineAirplanes.find(_.airplaneId == airplaneId) match {
          case Some(airlineAirplane) => Right(Got(airlineAirplane))
          case None                  => Left(EntryListEmpty)
        }
      }
      .value
      .execute
  }

  override def getAirlineAirplanesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineAirplane]]] =
    selectAirlineAirplanesBy(field, values, operator).asList(Some(field), Some(values)).execute

  override def getAirlineAirplanesByExternal[ET: TableBase, EV: Put](
    field: String,
    values: Nel[EV],
    operator: Operator
  ): F[ApiResult[List[AirlineAirplane]]] =
    selectAirlineAirplaneByExternal[ET, EV](field, values, operator)
      .asList(Some(field), Some(values))
      .execute

  override def getAirlineAirplanesByAirplane[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineAirplane]]] =
    getAirlineAirplanesByExternal[Airplane, V](field, values, operator)

  override def getAirlineAirplanesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineAirplane]]] =
    getAirlineAirplanesByExternal[Airline, V](field, values, operator)

  override def createAirlineAirplane(airlineAirplane: AirlineAirplaneCreate): F[ApiResult[Long]] =
    insertAirlineAirplane(airlineAirplane).attemptInsert.execute

  override def updateAirlineAirplane(
    airlineAirplane: AirlineAirplane
  ): F[ApiResult[Long]] =
    modifyAirlineAirplane(airlineAirplane).attemptUpdate(airlineAirplane.id).execute

  override def partiallyUpdateAirlineAirplane(
    id: Long,
    patch: AirlineAirplanePatch
  ): F[ApiResult[AirlineAirplane]] =
    EitherT(getAirlineAirplane(id)).flatMapF { airlineAirplaneOutput =>
      val airlineAirplane = airlineAirplaneOutput.value
      val patched = AirlineAirplane.fromPatch(id, patch, airlineAirplane)
      modifyAirlineAirplane(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirlineAirplane(id: Long): F[ApiResult[Unit]] =
    deleteAirlineAirplane(id).attemptDelete(id).execute
}

object AirlineAirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirlineAirplaneRepository[F]] =
    new AirlineAirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirlineAirplaneRepository[F]] =
    Resource.pure(new AirlineAirplaneRepository[F])
}
