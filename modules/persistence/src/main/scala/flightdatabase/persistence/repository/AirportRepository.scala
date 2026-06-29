package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.FieldValues
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.airport.Airport
import flightdatabase.airport.AirportAlgebra
import flightdatabase.airport.AirportCreate
import flightdatabase.airport.AirportPatch
import flightdatabase.city.City
import flightdatabase.country.Country
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.AirportRepository.PartiallyAppliedGetAllAirports
import flightdatabase.persistence.repository.AirportRepository.PartiallyAppliedGetByAirport
import flightdatabase.persistence.repository.AirportRepository.PartiallyAppliedGetByCity
import flightdatabase.persistence.repository.AirportRepository.PartiallyAppliedGetByCountry
import flightdatabase.persistence.repository.queries.AirportQueries._
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class AirportRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirportAlgebra[F] {

  override def doesAirportExist(id: Long): F[Boolean] = airportExists(id).unique.execute

  override def getAirports: PartiallyAppliedGetAll[F, Airport] =
    new PartiallyAppliedGetAllAirports[F]

  override def getAirport(id: Long): F[ApiResult[Airport]] =
    selectAirportsBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getAirportsBy: PartiallyAppliedGetBy[F, Airport] =
    new PartiallyAppliedGetByAirport[F]

  override def getAirportsByCity: PartiallyAppliedGetBy[F, Airport] =
    new PartiallyAppliedGetByCity[F]

  override def getAirportsByCountry: PartiallyAppliedGetBy[F, Airport] =
    new PartiallyAppliedGetByCountry[F]

  override def createAirport(airport: AirportCreate): F[ApiResult[Long]] =
    insertAirport(airport).attemptInsert.execute

  override def updateAirport(airport: Airport): F[ApiResult[Long]] =
    modifyAirport(airport).attemptUpdate(airport.id).execute

  override def partiallyUpdateAirport(id: Long, patch: AirportPatch): F[ApiResult[Airport]] =
    EitherT(getAirport(id)).flatMapF { airportOutput =>
      val airport = airportOutput.value
      val patched = Airport.fromPatch(id, patch, airport)
      modifyAirport(patched).attemptUpdate(patched).execute
    }.value

  override def removeAirport(id: Long): F[ApiResult[Unit]] =
    deleteAirport(id).attemptDelete(id).execute
}

object AirportRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[AirportRepository[F]] =
    new AirportRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, AirportRepository[F]] =
    Resource.pure(new AirportRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllAirports[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Airport] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Airport]]] =
      selectAllAirports(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Airport, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByAirport[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airport] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airport]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAirportsBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCity[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airport] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airport]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectAllAirportsByExternal[City, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCountry[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Airport] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Airport]]] = {
      implicit val put: Put[V] = fieldType.asPut
      EitherT(getFieldList[City, Long, Country, V]("id", FieldValues(field, values), operator))
        .flatMapF { cityIds =>
          val ids = cityIds.value
          selectAllAirportsByExternal[City, Long]("id", ids, Operator.In, sortAndLimit)
            .asNel(Some(field), Some(ids))
        }
        .value
        .execute
    }
  }
}
