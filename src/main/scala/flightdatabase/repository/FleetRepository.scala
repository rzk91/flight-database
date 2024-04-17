package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.fleet.Fleet
import flightdatabase.domain.fleet.FleetAlgebra
import flightdatabase.domain.fleet.FleetCreate
import flightdatabase.domain.fleet.FleetPatch
import flightdatabase.repository.queries.FleetQueries._
import flightdatabase.utils.implicits._

class FleetRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends FleetAlgebra[F] {

  override def doesFleetExist(id: Long): F[Boolean] = fleetExists(id).unique.execute

  override def getFleets: F[ApiResult[List[Fleet]]] = selectAllFleets.asList.execute

  override def getFleetsOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Fleet, String]("name").execute

  override def getFleet(id: Long): F[ApiResult[Fleet]] =
    selectFleetsBy("id", id).asSingle(id).execute

  override def getFleets[V: Put](field: String, value: V): F[ApiResult[List[Fleet]]] =
    selectFleetsBy(field, value).asList.execute

  override def getFleetByHubAirportIata(hubAirportIata: String): F[ApiResult[List[Fleet]]] =
    selectFleetByExternal[Airport, String]("iata", hubAirportIata).asList.execute

  override def getFleetByHubAirportIcao(hubAirportIcao: String): F[ApiResult[List[Fleet]]] =
    selectFleetByExternal[Airport, String]("icao", hubAirportIcao).asList.execute

  override def createFleet(fleet: FleetCreate): F[ApiResult[Long]] =
    insertFleet(fleet).attemptInsert.execute

  override def updateFleet(fleet: Fleet): F[ApiResult[Fleet]] =
    modifyFleet(fleet).attemptUpdate(fleet).execute

  override def partiallyUpdateFleet(id: Long, patch: FleetPatch): F[ApiResult[Fleet]] =
    EitherT(getFleet(id)).flatMapF { fleetOutput =>
      val fleet = fleetOutput.value
      updateFleet(Fleet.fromPatch(id, patch, fleet))
    }.value

  override def removeFleet(id: Long): F[ApiResult[Unit]] =
    deleteFleet(id).attemptDelete(id).execute
}

object FleetRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[FleetRepository[F]] =
    new FleetRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, FleetRepository[F]] =
    Resource.pure(new FleetRepository[F])
}
