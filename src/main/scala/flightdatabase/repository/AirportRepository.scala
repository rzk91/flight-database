package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.airport.Airport
import flightdatabase.domain.airport.AirportAlgebra
import flightdatabase.domain.airport.AirportCreate
import flightdatabase.domain.airport.AirportPatch
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.repository.queries.AirportQueries._
import flightdatabase.utils.FieldValues
import flightdatabase.utils.implicits._

class AirportRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends AirportAlgebra[F] {

  override def doesAirportExist(id: Long): F[Boolean] = airportExists(id).unique.execute

  override def getAirports: F[ApiResult[Nel[Airport]]] =
    selectAllAirports.asNel().execute

  override def getAirportsOnly[V: Read](field: String): F[ApiResult[Nel[V]]] =
    getFieldList[Airport, V](field).execute

  override def getAirport(id: Long): F[ApiResult[Airport]] =
    selectAirportsBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getAirportsBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]] =
    selectAirportsBy(field, values, operator).asNel(Some(field), Some(values)).execute

  def getAirportsByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]] =
    selectAllAirportsByExternal[City, V](field, values, operator)
      .asNel(Some(field), Some(values))
      .execute

  def getAirportsByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Airport]]] =
    EitherT(getFieldList[City, Long, Country, V]("id", FieldValues(field, values), operator))
      .flatMapF { cityIds =>
        val ids = cityIds.value
        selectAllAirportsByExternal[City, Long]("id", ids, Operator.In).asNel(
          Some(field),
          Some(ids)
        )
      }
      .value
      .execute

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
}
