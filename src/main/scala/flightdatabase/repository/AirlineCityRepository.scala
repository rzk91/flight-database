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
import flightdatabase.domain.airline_city._
import flightdatabase.domain.city.City
import flightdatabase.repository.queries.AirlineCityQueries._
import flightdatabase.utils.implicits._

class AirlineCityRepository[F[_]: Concurrent] private (implicit transactor: Transactor[F])
    extends AirlineCityAlgebra[F] {

  override def doesAirlineCityExist(id: Long): F[Boolean] =
    airlineCityExists(id).unique.execute

  override def getAirlineCities: F[ApiResult[List[AirlineCity]]] =
    selectAllAirlineCities.asList().execute

  override def getAirlineCity(id: Long): F[ApiResult[AirlineCity]] =
    selectAirlineCitiesBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]] =
    EitherT(
      selectAirlineCitiesBy("airline_id", Nel.one(airlineId), Operator.Equals)
        .asList(invalidValue = Some(airlineId))
    ).subflatMap[ApiError, ApiOutput[AirlineCity]] { output =>
        val airlineCities = output.value
        airlineCities.find(_.cityId == cityId) match {
          case Some(airlineCity) => Right(Got(airlineCity))
          case None              => Left(EntryListEmpty)
        }
      }
      .value
      .execute

  override def getAirlineCities[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]] =
    selectAirlineCitiesBy(field, values, operator).asList(Some(field), Some(values)).execute

  override def getAirlineCitiesByExternal[ET: TableBase, EV: Put](
    field: String,
    values: Nel[EV],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]] =
    selectAirlineCityByExternal(field, values, operator).asList(Some(field), Some(values)).execute

  override def getAirlineCitiesByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]] =
    getAirlineCitiesByExternal[City, V](field, values, operator)

  override def getAirlineCitiesByAirline[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[AirlineCity]]] =
    getAirlineCitiesByExternal[Airline, V](field, values, operator)

  override def createAirlineCity(airlineCity: AirlineCityCreate): F[ApiResult[Long]] =
    insertAirlineCity(airlineCity).attemptInsert.execute

  override def updateAirlineCity(airlineCity: AirlineCity): F[ApiResult[Long]] =
    modifyAirlineCity(airlineCity).attemptUpdate(airlineCity.id).execute

  override def partiallyUpdateAirlineCity(
    id: Long,
    patch: AirlineCityPatch
  ): F[ApiResult[AirlineCity]] =
    EitherT(getAirlineCity(id)).flatMapF { airlineCityOutput =>
      val airlineCity = airlineCityOutput.value
      val patched = AirlineCity.fromPatch(id, patch, airlineCity)
      modifyAirlineCity(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirlineCity(id: Long): F[ApiResult[Unit]] =
    deleteAirlineCity(id).attemptDelete(id).execute
}

object AirlineCityRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirlineCityRepository[F]] =
    new AirlineCityRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirlineCityRepository[F]] =
    Resource.pure(new AirlineCityRepository[F])
}
