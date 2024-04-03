package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Fragment
import doobie.hikari.HikariTransactor
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.FlightDbTable.AIRPLANE
import flightdatabase.domain.FlightDbTable.MANUFACTURER
import flightdatabase.domain.airplane.AirplaneAlgebra
import flightdatabase.domain.airplane.AirplaneModel
import flightdatabase.utils.implicits._

// TODO: Perhaps replace the resource with a simple instance of `Transactor[F]`
// Question: how does it then work with pooling then?
class AirplaneRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends AirplaneAlgebra[F] {

  override def getAirplanes(
    maybeManufacturer: Option[String]
  ): F[ApiResult[List[AirplaneModel]]] = {
    val allAirplanes =
      fr"SELECT a.id, a.name, m.name, a.capacity, a.max_range_in_km" ++
        fr"FROM airplane a INNER JOIN manufacturer m on a.manufacturer_id = m.id"

    val addManufacturer = maybeManufacturer.fold(Fragment.empty)(m => fr"WHERE m.name = $m")

    (allAirplanes ++ addManufacturer).query[AirplaneModel].to[List].map(liftListToApiResult).execute
  }

  override def getAirplanesOnlyNames(
    maybeManufacturer: Option[String]
  ): F[ApiResult[List[String]]] =
    getStringListBy(AIRPLANE, MANUFACTURER, maybeManufacturer).execute

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
