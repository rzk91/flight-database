package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.ApiError
import flightdatabase.ApiOutput
import flightdatabase.ApiResult
import flightdatabase.EntryNotFound
import flightdatabase.FieldType
import flightdatabase.Got
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airline.Airline
import flightdatabase.airline_city._
import flightdatabase.city.City
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirlineCityRepository.PartiallyAppliedGetAllAirlineCities
import flightdatabase.persistence.repository.AirlineCityRepository.PartiallyAppliedGetByAirline
import flightdatabase.persistence.repository.AirlineCityRepository.PartiallyAppliedGetByAirlineCity
import flightdatabase.persistence.repository.AirlineCityRepository.PartiallyAppliedGetByCity
import flightdatabase.persistence.repository.queries.AirlineCityQueries._
import flightdatabase.persistence.syntax.all._

class AirlineCityRepository[F[_]: Concurrent] private (implicit transactor: Transactor[F])
    extends AirlineCityAlgebra[F] {

  override def doesAirlineCityExist(id: Long): F[Boolean] =
    airlineCityExists(id).unique.execute

  override def getAirlineCities: PartiallyAppliedGetAll[F, AirlineCity] =
    new PartiallyAppliedGetAllAirlineCities[F]

  override def getAirlineCity(id: Long): F[ApiResult[AirlineCity]] =
    selectAirlineCitiesBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirlineCity(airlineId: Long, cityId: Long): F[ApiResult[AirlineCity]] = {
    val idAsNel = Nel.one(airlineId)
    EitherT(
      selectAirlineCitiesBy("airline_id", idAsNel, Operator.Equals, ValidatedSortAndLimit.empty)
        .asNel(invalidValues = Some(idAsNel))
    ).subflatMap[ApiError, ApiOutput[AirlineCity]] { output =>
        val airlineCities = output.value
        airlineCities.find(_.cityId == cityId) match {
          case Some(airlineCity) => Right(Got(airlineCity))
          case None              => Left(EntryNotFound((airlineId, cityId)))
        }
      }
      .value
      .execute
  }

  override def getAirlineCitiesBy: PartiallyAppliedGetBy[F, AirlineCity] =
    new PartiallyAppliedGetByAirlineCity[F]

  override def getAirlineCitiesByCity: PartiallyAppliedGetBy[F, AirlineCity] =
    new PartiallyAppliedGetByCity[F]

  override def getAirlineCitiesByAirline: PartiallyAppliedGetBy[F, AirlineCity] =
    new PartiallyAppliedGetByAirline[F]

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

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirlineCities[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, AirlineCity] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[AirlineCity]]] =
      selectAllAirlineCities(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[AirlineCity, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirlineCity[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineCity] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineCity]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineCitiesBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCity[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineCity] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineCity]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineCityByExternal[City, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByAirline[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, AirlineCity] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[AirlineCity]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirlineCityByExternal[Airline, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
