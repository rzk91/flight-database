package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Query0
import doobie.Read
import doobie.Transactor
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
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

  override def getAirlineRoutes: F[ApiResult[Nel[AirlineRoute]]] =
    selectAllAirlineRoutes.asNel().execute

  override def getAirlineRoutesOnly[V: Read](field: String): F[ApiResult[Nel[V]]] =
    getFieldList[AirlineRoute, V](field).execute

  override def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]] =
    selectAirlineRouteBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirlineRoutesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]] =
    selectAirlineRouteBy(field, values, operator).asNel(Some(field), Some(values)).execute

  override def getAirlineRoutesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]] =
    // Get airline IDs for given airline field values
    EitherT(
      selectWhereQuery[Airline, Long, V]("id", field, values, operator)
        .asNel(Some(field), Some(values))
    ).flatMapF { airlineIds =>
        val ids = airlineIds.value
        selectAirlineRoutesByExternal[AirlineAirplane, Long]("airline_id", ids, Operator.In)
          .asNel(invalidValues = Some(ids))
      }
      .value
      .execute

  override def getAirlineRoutesByAirplane[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[AirlineRoute]]] =
    EitherT(
      selectWhereQuery[Airplane, Long, V]("id", field, values, operator)
        .asNel(Some(field), Some(values))
    ).flatMapF { airplaneIds =>
        val ids = airplaneIds.value
        selectAirlineRoutesByExternal[AirlineAirplane, Long]("airplane_id", ids, Operator.In)
          .asNel(invalidValues = Some(ids))
      }
      .value
      .execute

  override def getAirlineRoutesByAirport[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    inbound: Option[Boolean] // None for both inbound and outbound
  ): F[ApiResult[Nel[AirlineRoute]]] = {
    def q(f: String): Query0[AirlineRoute] =
      selectAirlineRoutesByExternal[Airport, V](field, values, operator, Some(f))
    inbound.fold {
      val startQuery = q("start_airport_id")
      val destinationQuery = q("destination_airport_id")
      (startQuery.toFragment ++ fr"UNION" ++ destinationQuery.toFragment).query[AirlineRoute]
    }(in => q(if (in) "destination_airport_id" else "start_airport_id"))
  }.asNel(Some(field), Some(values)).execute

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
