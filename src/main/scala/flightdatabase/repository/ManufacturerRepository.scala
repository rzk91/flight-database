package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
import flightdatabase.domain.manufacturer.ManufacturerCreate
import flightdatabase.domain.manufacturer.ManufacturerPatch
import flightdatabase.repository.queries.ManufacturerQueries._
import flightdatabase.utils.FieldValue
import flightdatabase.utils.implicits._

class ManufacturerRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends ManufacturerAlgebra[F] {

  override def doesManufacturerExist(id: Long): F[Boolean] =
    manufacturerExists(id).unique.execute

  override def getManufacturers: F[ApiResult[List[Manufacturer]]] =
    selectAllManufacturers.asList.execute

  override def getManufacturersOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Manufacturer, String]("name").execute

  override def getManufacturer(id: Long): F[ApiResult[Manufacturer]] =
    selectManufacturersBy("id", id).asSingle(id).execute

  override def getManufacturers[V: Put](field: String, value: V): F[ApiResult[List[Manufacturer]]] =
    selectManufacturersBy(field, value).asList.execute

  override def getManufacturersByCity(city: String): F[ApiResult[List[Manufacturer]]] =
    selectManufacturersByExternal[City, String]("name", city).asList.execute

  override def getManufacturersByCountry(country: String): F[ApiResult[List[Manufacturer]]] =
    EitherT(
      getFieldList[City, String, Country, String]("name", FieldValue("name", country))
    ).flatMapF {
        _.value
          .flatTraverse(selectManufacturersByExternal[City, String]("name", _).to[List])
          .map(liftListToApiResult)
      }
      .value
      .execute

  override def createManufacturer(manufacturer: ManufacturerCreate): F[ApiResult[Long]] =
    insertManufacturer(manufacturer).attemptInsert.execute

  override def updateManufacturer(manufacturer: Manufacturer): F[ApiResult[Manufacturer]] =
    modifyManufacturer(manufacturer).attemptUpdate(manufacturer).execute

  override def partiallyUpdateManufacturer(
    id: Long,
    patch: ManufacturerPatch
  ): F[ApiResult[Manufacturer]] =
    EitherT(getManufacturer(id)).flatMapF { manufacturerOutput =>
      val manufacturer = manufacturerOutput.value
      updateManufacturer(Manufacturer.fromPatch(id, patch, manufacturer))
    }.value

  override def removeManufacturer(id: Long): F[ApiResult[Unit]] =
    deleteManufacturer(id).attemptDelete(id).execute
}

object ManufacturerRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[ManufacturerRepository[F]] =
    new ManufacturerRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, ManufacturerRepository[F]] =
    Resource.pure(new ManufacturerRepository[F])
}
