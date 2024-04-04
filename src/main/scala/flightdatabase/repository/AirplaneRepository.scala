package flightdatabase.repository

import cats.effect.{Concurrent, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.FlightDbTable.{AIRPLANE, MANUFACTURER}
import flightdatabase.domain.airplane.{AirplaneAlgebra, AirplaneModel}
import flightdatabase.repository.queries.AirplaneQueries._
import flightdatabase.utils.implicits._
import flightdatabase.utils.TableValue

// TODO: Perhaps replace the resource with a simple instance of `Transactor[F]`
// Question: how does it then work with pooling then?
class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends AirplaneAlgebra[F] {

  override def getAirplanes(
    maybeManufacturer: Option[String]
  ): F[ApiResult[List[AirplaneModel]]] =
    selectAllAirplanes(maybeManufacturer).asList.execute

  override def getAirplanesOnlyNames(
    maybeManufacturer: Option[String]
  ): F[ApiResult[List[String]]] =
    getNameList(AIRPLANE, maybeManufacturer.map(TableValue(MANUFACTURER, _))).execute

  override def getAirplane(id: Long): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]

  override def createAirplane(airplane: AirplaneModel): F[ApiResult[Long]] =
    featureNotImplemented[F, Long]

  override def updateAirplane(airplane: AirplaneModel): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]

  override def deleteAirplane(id: Long): F[ApiResult[AirplaneModel]] =
    featureNotImplemented[F, AirplaneModel]
}

object AirplaneRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[AirplaneRepository[F]] =
    new AirplaneRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, AirplaneRepository[F]] =
    Resource.pure(new AirplaneRepository[F])
}
