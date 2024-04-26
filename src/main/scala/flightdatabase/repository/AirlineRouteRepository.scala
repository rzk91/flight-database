package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Query0
import doobie.Transactor
import doobie.implicits._
import flightdatabase.domain.ApiError
import flightdatabase.domain.ApiOutput
import flightdatabase.domain.ApiResult
import flightdatabase.domain.Got
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
    selectAllAirlineRoutes.asList.execute

  override def getAirlineRoutesOnlyRoutes: F[ApiResult[List[String]]] =
    getFieldList[AirlineRoute, String]("route_number").execute

  override def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]] =
    selectAirlineRouteBy("id", id).asSingle(id).execute

  override def getAirlineRoutes[V: Put](field: String, value: V): F[ApiResult[List[AirlineRoute]]] =
    selectAirlineRouteBy(field, value).asList.execute

  override def getAirlineRoutesByAirlineId(airlineId: Long): F[ApiResult[List[AirlineRoute]]] =
    selectAirlineRoutesByExternal[AirlineAirplane, Long]("airline_id", airlineId).asList.execute

  override def getAirlineRoutesByAirline[V: Put](
    field: String,
    value: V
  ): F[ApiResult[List[AirlineRoute]]] =
    // Get airline IDs for given airline field values
    EitherT(selectWhereQuery[Airline, Long, V]("id", field, value).asList.execute)
      .flatMap[ApiError, ApiOutput[List[AirlineRoute]]] { airlineIdsOutput =>
        // Accumulate all airline_routes for each airline_airplane_id based on each airline_id
        val airlineIds = airlineIdsOutput.value
        airlineIds
          .foldLeft(EitherT.pure[F, ApiError](List.empty[AirlineRoute])) { (acc, id) =>
            for {
              accRoutes <- acc
              routes    <- EitherT(getAirlineRoutesByAirlineId(id))
            } yield accRoutes ++ routes.value
          }
          .map(Got(_)) // Convert to ApiOutput
      }
      .value

  override def getAirlineRoutesByAirplaneId(airplaneId: Long): F[ApiResult[List[AirlineRoute]]] =
    selectAirlineRoutesByExternal[AirlineAirplane, Long]("airplane_id", airplaneId).asList.execute

  override def getAirlineRoutesByAirplane[V: Put](
    field: String,
    value: V
  ): F[ApiResult[List[AirlineRoute]]] =
    EitherT(selectWhereQuery[Airplane, Long, V]("id", field, value).asList.execute)
      .flatMap[ApiError, ApiOutput[List[AirlineRoute]]] { airplaneIdsOutput =>
        val airplaneIds = airplaneIdsOutput.value
        airplaneIds
          .foldLeft(EitherT.pure[F, ApiError](List.empty[AirlineRoute])) { (acc, id) =>
            for {
              accRoutes <- acc
              routes    <- EitherT(getAirlineRoutesByAirplaneId(id))
            } yield accRoutes ++ routes.value
          }
          .map(Got(_))
      }
      .value

  override def getAirlineRoutesByAirport[V: Put](
    field: String,
    value: V,
    inbound: Option[Boolean]
  ): F[ApiResult[List[AirlineRoute]]] = {
    def q(f: String): Query0[AirlineRoute] =
      selectAirlineRoutesByExternal[Airport, V](field, value, Some(f))
    inbound.fold {
      val startQuery = q("start_airport_id")
      val destinationQuery = q("destination_airport_id")
      (startQuery.toFragment ++ fr"UNION" ++ destinationQuery.toFragment).query[AirlineRoute]
    }(in => q(if (in) "start_airport_id" else "destination_airport_id"))
  }.asList.execute

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
