package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Query0
import doobie.Transactor
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.airline.Airline
import flightdatabase.domain.airline_airplane.AirlineAirplane
import flightdatabase.domain.airline_route.AirlineRoute
import flightdatabase.domain.airline_route.AirlineRouteAlgebra
import flightdatabase.domain.airline_route.AirlineRouteCreate
import flightdatabase.domain.airline_route.AirlineRoutePatch
import flightdatabase.domain.airplane.Airplane
import flightdatabase.domain.airport.Airport
import flightdatabase.repository.queries.AirlineRouteQueries._
import flightdatabase.repository.queries.selectWhereQuery
import flightdatabase.utils.implicits._

class AirlineRouteRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirlineRouteAlgebra[F] {

  override def doesAirlineRouteExist(id: Long): F[Boolean] =
    airlineRouteExists(id).unique.execute

  override def getAirlineRoutes: F[ApiResult[List[AirlineRoute]]] =
    selectAllAirlineRoutes.asList().execute

  override def getAirlineRoutesOnlyRoutes: F[ApiResult[List[String]]] =
    getFieldList[AirlineRoute, String]("route_number").execute

  override def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]] =
    selectAirlineRouteBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirlineRoutesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineRoute]]] =
    selectAirlineRouteBy(field, values, operator).asList(Some(field), Some(values)).execute

  override def getAirlineRoutesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineRoute]]] =
    // Get airline IDs for given airline field values
    EitherT(
      selectWhereQuery[Airline, Long, V]("id", field, values, operator)
        .asList(Some(field), Some(values))
        .execute
    ).flatMapF(aIds => getAirlineRoutesByAirlineIds(aIds.value)).value

  override def getAirlineRoutesByAirplane[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineRoute]]] =
    EitherT(
      selectWhereQuery[Airplane, Long, V]("id", field, values, operator)
        .asList(Some(field), Some(values))
        .execute
    ).flatMapF(aIds => getAirlineRoutesByAirplaneIds(aIds.value)).value

  override def getAirlineRoutesByAirport[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    inbound: Option[Boolean] // None for both inbound and outbound
  ): F[ApiResult[List[AirlineRoute]]] = {
    def q(f: String): Query0[AirlineRoute] =
      selectAirlineRoutesByExternal[Airport, V](field, values, operator, Some(f))
    inbound.fold {
      val startQuery = q("start_airport_id")
      val destinationQuery = q("destination_airport_id")
      (startQuery.toFragment ++ fr"UNION" ++ destinationQuery.toFragment).query[AirlineRoute]
    }(in => q(if (in) "destination_airport_id" else "start_airport_id"))
  }.asList(Some(field), Some(values)).execute

  override def createAirlineRoute(airlineRoute: AirlineRouteCreate): F[ApiResult[Long]] =
    insertAirlineRoute(airlineRoute).attemptInsert.execute

  override def updateAirlineRoute(airlineRoute: AirlineRoute): F[ApiResult[Long]] =
    modifyAirlineRoute(airlineRoute).attemptUpdate(airlineRoute.id).execute

  override def partiallyUpdateAirlineRoute(
    id: Long,
    patch: AirlineRoutePatch
  ): F[ApiResult[AirlineRoute]] =
    EitherT(getAirlineRoute(id)).flatMapF { airlineRouteOutput =>
      val airlineRoute = airlineRouteOutput.value
      val patched = AirlineRoute.fromPatch(id, patch, airlineRoute)
      modifyAirlineRoute(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirlineRoute(id: Long): F[ApiResult[Unit]] =
    deleteAirlineRoute(id).attemptDelete(id).execute

  private def getAirlineRoutesByAirlineIds(
    airlineIds: List[Long]
  ): F[ApiResult[List[AirlineRoute]]] =
    Nel.fromList(airlineIds) match {
      case Some(ids) =>
        selectAirlineRoutesByExternal[AirlineAirplane, Long]("airline_id", ids, Operator.In)
          .asList(invalidValues = Some(ids))
          .execute
      case None => EntryListEmpty.elevate[F, List[AirlineRoute]]
    }

  private def getAirlineRoutesByAirplaneIds(
    airplaneIds: List[Long]
  ): F[ApiResult[List[AirlineRoute]]] =
    Nel.fromList(airplaneIds) match {
      case Some(ids) =>
        selectAirlineRoutesByExternal[AirlineAirplane, Long]("airplane_id", ids, Operator.In)
          .asList(invalidValues = Some(ids))
          .execute
      case None => EntryListEmpty.elevate[F, List[AirlineRoute]]
    }
}

object AirlineRouteRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirlineRouteRepository[F]] =
    new AirlineRouteRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirlineRouteRepository[F]] =
    Resource.pure(new AirlineRouteRepository[F])
}
