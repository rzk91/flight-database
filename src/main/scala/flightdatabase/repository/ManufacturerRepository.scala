package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
import flightdatabase.domain.manufacturer.ManufacturerModel
import flightdatabase.repository.queries.ManufacturerQueries._
import flightdatabase.utils.implicits._

class ManufacturerRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends ManufacturerAlgebra[F] {

  override def getManufacturers: F[ApiResult[List[ManufacturerModel]]] =
    selectAllManufacturers.asList.execute

  override def getManufacturerOnlyNames: F[ApiResult[List[String]]] =
    getNameList[ManufacturerModel].execute

  override def getManufacturer(id: Long): F[ApiResult[ManufacturerModel]] =
    featureNotImplemented[F, ManufacturerModel]

  override def getManufacturersByCity(city: String): F[ApiResult[List[ManufacturerModel]]] =
    featureNotImplemented[F, List[ManufacturerModel]]

  override def getManufacturersByCountry(country: String): F[ApiResult[List[ManufacturerModel]]] =
    featureNotImplemented[F, List[ManufacturerModel]]

  override def createManufacturer(manufacturer: ManufacturerModel): F[ApiResult[Long]] =
    insertManufacturer(manufacturer).attemptInsert.execute

  override def updateManufacturer(
    manufacturer: ManufacturerModel
  ): F[ApiResult[ManufacturerModel]] = featureNotImplemented[F, ManufacturerModel]

  override def removeManufacturer(id: Long): F[ApiResult[Unit]] =
    deleteManufacturer(id).attemptDelete.execute
}

object ManufacturerRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[ManufacturerRepository[F]] =
    new ManufacturerRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, ManufacturerRepository[F]] =
    Resource.pure(new ManufacturerRepository[F])
}