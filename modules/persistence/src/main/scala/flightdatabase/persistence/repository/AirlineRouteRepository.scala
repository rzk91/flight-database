package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Query0
import doobie.Read
import doobie.Transactor
import doobie.syntax.string._
import flightdatabase.ApiResult
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airline.Airline
import flightdatabase.airline_airplane.AirlineAirplane
import flightdatabase.airline_route.AirlineRoute
import flightdatabase.airline_route.AirlineRouteAlgebra
import flightdatabase.airline_route.AirlineRouteCreate
import flightdatabase.airline_route.AirlineRoutePatch
import flightdatabase.airplane.Airplane
import flightdatabase.airport.Airport
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirlineRouteRepository.PartiallyAppliedGetAllAirlineRoutes
import flightdatabase.persistence.repository.AirlineRouteRepository.PartiallyAppliedGetByAirline
import flightdatabase.persistence.repository.AirlineRouteRepository.PartiallyAppliedGetByAirlineRoute
import flightdatabase.persistence.repository.AirlineRouteRepository.PartiallyAppliedGetByAirplane
import flightdatabase.persistence.repository.AirlineRouteRepository.PartiallyAppliedGetByAirport
import flightdatabase.persistence.repository.queries.AirlineRouteQueries._
import flightdatabase.persistence.repository.queries.selectWhereQuery
import flightdatabase.persistence.syntax.all._

class AirlineRouteRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirlineRouteAlgebra[F] {

  override def doesAirlineRouteExist(id: Long): F[Boolean] =
    airlineRouteExists(id).unique.execute

  override def getAirlineRoutes: PartiallyAppliedGetAll[F, AirlineRoute] =
    new PartiallyAppliedGetAllAirlineRoutes[F]

  override def getAirlineRoute(id: Long): F[ApiResult[AirlineRoute]] =
    selectAirlineRouteBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirlineRoutesBy: PartiallyAppliedGetBy[F, AirlineRoute] =
    new PartiallyAppliedGetByAirlineRoute[F]

  override def getAirlineRoutesByAirline: PartiallyAppliedGetBy[F, AirlineRoute] =
    new PartiallyAppliedGetByAirline[F]

  override def getAirlineRoutesByAirplane: PartiallyAppliedGetBy[F, AirlineRoute] =
    new PartiallyAppliedGetByAirplane[F]

  override def getAirlineRoutesByAirport(
    inbound: Option[Boolean]
  ): PartiallyAppliedGetBy[F, AirlineRoute] =
    new PartiallyAppliedGetByAirport[F](inbound)

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

  // Needed by the airport UNION query, which calls `.query[AirlineRoute]` directly
  implicit private val readAirlineRoute: Read[AirlineRoute] = Read.derived

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirlineRoutes[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, AirlineRoute] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[AirlineRoute]]] =
      selectAllAirlineRoutes(sortAndLimit).asNel().execute

    override def apply[V: Read](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String
    ): F[ApiResult[Nel[V]]] =
      getFieldList2[AirlineRoute, V](sortAndLimit, returnField).execute
  }

  private class PartiallyAppliedGetByAirlineRoute[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V: Put](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit
    ): F[ApiResult[Nel[AirlineRoute]]] =
      selectAirlineRouteBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
  }

  private class PartiallyAppliedGetByAirline[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V: Put](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit
    ): F[ApiResult[Nel[AirlineRoute]]] =
      // Get airline IDs for given airline field values, then route through airline_airplane
      EitherT(
        selectWhereQuery[Airline, Long, V]("id", field, values, operator)
          .asNel(Some(field), Some(values))
      ).flatMapF { airlineIds =>
          val ids = airlineIds.value
          selectAirlineRoutesByExternal[AirlineAirplane, Long](
            "airline_id",
            ids,
            Operator.In,
            sortAndLimit
          ).asNel(invalidValues = Some(ids))
        }
        .value
        .execute
  }

  private class PartiallyAppliedGetByAirplane[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V: Put](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit
    ): F[ApiResult[Nel[AirlineRoute]]] =
      EitherT(
        selectWhereQuery[Airplane, Long, V]("id", field, values, operator)
          .asNel(Some(field), Some(values))
      ).flatMapF { airplaneIds =>
          val ids = airplaneIds.value
          selectAirlineRoutesByExternal[AirlineAirplane, Long](
            "airplane_id",
            ids,
            Operator.In,
            sortAndLimit
          ).asNel(invalidValues = Some(ids))
        }
        .value
        .execute
  }

  private class PartiallyAppliedGetByAirport[F[_]: Concurrent](
    inbound: Option[Boolean] // None for both inbound and outbound
  )(implicit transactor: Transactor[F])
      extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V: Put](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit
    ): F[ApiResult[Nel[AirlineRoute]]] = {
      def q(f: String, sl: ValidatedSortAndLimit): Query0[AirlineRoute] =
        selectAirlineRoutesByExternal[Airport, V](field, values, operator, sl, Some(f))
      inbound.fold {
        // For the UNION, keep the legs unsorted and apply sort/limit to the whole result
        val startQuery = q("start_airport_id", ValidatedSortAndLimit.empty)
        val destinationQuery = q("destination_airport_id", ValidatedSortAndLimit.empty)
        (startQuery.toFragment ++ fr"UNION" ++ destinationQuery.toFragment ++ sortAndLimit.fragment)
          .query[AirlineRoute]
      }(in => q(if (in) "destination_airport_id" else "start_airport_id", sortAndLimit))
    }.asNel(Some(field), Some(values)).execute
  }
}
