package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import flightdatabase.ApiResult
import flightdatabase.FieldType
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
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class AirlineRouteRepository[F[_]: Concurrent] private (implicit
  transactor: Transactor[F]
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

  def make[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): F[AirlineRouteRepository[F]] =
    new AirlineRouteRepository[F].pure[F]

  def resource[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): Resource[F, AirlineRouteRepository[F]] =
    Resource.pure(new AirlineRouteRepository[F])

  // Needed by the airport UNION query, which calls `.query[AirlineRoute]` directly
  implicit private val readAirlineRoute: Read[AirlineRoute] = Read.derived

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirlineRoutes[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, AirlineRoute] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[AirlineRoute]]] =
      selectAllAirlineRoutes(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[AirlineRoute, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirlineRoute[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineRoute]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineRouteBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByAirline[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineRoute]]] = {
      implicit val put: Put[V] = fieldType.asPut
      // Get airline IDs for given airline field values, then route through airline_airplane
      EitherT(
        selectWhereQuery[Airline, Long, V]("id", field, values, operator)
          .asNel(Some(field), Some(values))
      ).flatMapF { airlineIds =>
        val ids = airlineIds.value
        selectAirlineRoutesByExternal[AirlineAirplane, Long](
          Nel.of("airline_airplane_id"),
          "airline_id",
          ids,
          Operator.In,
          sortAndLimit
        ).asNel(invalidValues = Some(ids))
      }.value
        .execute
    }
  }

  private class PartiallyAppliedGetByAirplane[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineRoute]]] = {
      implicit val put: Put[V] = fieldType.asPut
      EitherT(
        selectWhereQuery[Airplane, Long, V]("id", field, values, operator)
          .asNel(Some(field), Some(values))
      ).flatMapF { airplaneIds =>
        val ids = airplaneIds.value
        selectAirlineRoutesByExternal[AirlineAirplane, Long](
          Nel.of("airline_airplane_id"),
          "airplane_id",
          ids,
          Operator.In,
          sortAndLimit
        ).asNel(invalidValues = Some(ids))
      }.value
        .execute
    }
  }

  private class PartiallyAppliedGetByAirport[F[_]: Concurrent](
    inbound: Option[Boolean] // None for both inbound and outbound
  )(implicit transactor: Transactor[F])
      extends PartiallyAppliedGetBy[F, AirlineRoute] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineRoute]]] = {
      implicit val put: Put[V] = fieldType.asPut
      val airlineRouteFields = {
        inbound match {
          case Some(true)  => Nel.of("destination_airport_id")
          case Some(false) => Nel.of("start_airport_id")
          case None        => Nel.of("start_airport_id", "destination_airport_id")
        }
      }

      selectAirlineRoutesByExternal[Airport, V](
        airlineRouteFields,
        field,
        values,
        operator,
        sortAndLimit
      ).asNel(Some(field), Some(values)).execute
    }
  }
}
