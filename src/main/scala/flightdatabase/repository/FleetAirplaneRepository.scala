package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.TableBase
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet_airplane.FleetAirplane
import flightdatabase.domain.fleet_airplane.FleetAirplaneAlgebra
import flightdatabase.domain.fleet_airplane.FleetAirplaneCreate
import flightdatabase.domain.fleet_airplane.FleetAirplanePatch
import flightdatabase.repository.queries.FleetAirplaneQueries._
import flightdatabase.utils.implicits._

class FleetAirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends FleetAirplaneAlgebra[F] {

  override def doesFleetAirplaneExist(id: Long): F[Boolean] = fleetAirplaneExists(id).unique.execute

  override def getFleetAirplanes: F[ApiResult[List[FleetAirplane]]] =
    selectAllFleetAirplanes.asList.execute

  override def getFleetAirplane(id: Long): F[ApiResult[FleetAirplane]] =
    selectFleetAirplanesBy("id", id).asSingle(id).execute

  override def getFleetAirplanes[V: Put](
    field: String,
    value: V
  ): F[ApiResult[List[FleetAirplane]]] =
    selectFleetAirplanesBy(field, value).asList.execute

  override def getFleetAirplanesByExternal[ET: TableBase, EV: Put](
    field: String,
    value: EV
  ): F[ApiResult[List[FleetAirplane]]] =
    selectFleetAirplaneByExternal[ET, EV](field, value).asList.execute

  override def getFleetAirplanesByAirplaneName(
    airplaneName: String
  ): F[ApiResult[List[FleetAirplane]]] =
    getFleetAirplanesByExternal[Airplane, String]("name", airplaneName)

  override def getFleetAirplanesByFleetName(
    fleetName: String
  ): F[ApiResult[List[FleetAirplane]]] =
    getFleetAirplanesByExternal[Fleet, String]("name", fleetName)

  override def createFleetAirplane(fleetAirplane: FleetAirplaneCreate): F[ApiResult[Long]] =
    insertFleetAirplane(fleetAirplane).attemptInsert.execute

  override def updateFleetAirplane(
    fleetAirplane: FleetAirplane
  ): F[ApiResult[Long]] =
    modifyFleetAirplane(fleetAirplane).attemptUpdate(fleetAirplane.id).execute

  override def partiallyUpdateFleetAirplane(
    id: Long,
    patch: FleetAirplanePatch
  ): F[ApiResult[FleetAirplane]] =
    EitherT(getFleetAirplane(id)).flatMapF { fleetAirplaneOutput =>
      val fleetAirplane = fleetAirplaneOutput.value
      val patched = FleetAirplane.fromPatch(id, patch, fleetAirplane)
      modifyFleetAirplane(patched).attemptUpdate(patched).execute
    }.value

  override def removeFleetAirplane(id: Long): F[ApiResult[Unit]] =
    deleteFleetAirplane(id).attemptDelete(id).execute
}

object FleetAirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[FleetAirplaneRepository[F]] =
    new FleetAirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, FleetAirplaneRepository[F]] =
    Resource.pure(new FleetAirplaneRepository[F])
}
