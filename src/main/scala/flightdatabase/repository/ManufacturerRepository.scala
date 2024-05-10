package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.ConnectionIO
import doobie.Put
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult
import flightdatabase.domain.EntryListEmpty
import flightdatabase.domain.city.City
import flightdatabase.domain.country.Country
import flightdatabase.domain.manufacturer.Manufacturer
import flightdatabase.domain.manufacturer.ManufacturerAlgebra
import flightdatabase.domain.manufacturer.ManufacturerCreate
import flightdatabase.domain.manufacturer.ManufacturerPatch
import flightdatabase.repository.queries.ManufacturerQueries._
import flightdatabase.utils.FieldValues
import flightdatabase.utils.implicits._

class ManufacturerRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends ManufacturerAlgebra[F] {

  override def doesManufacturerExist(id: Long): F[Boolean] =
    manufacturerExists(id).unique.execute

  override def getManufacturers: F[ApiResult[List[Manufacturer]]] =
    selectAllManufacturers.asList().execute

  override def getManufacturersOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Manufacturer, String]("name").execute

  override def getManufacturer(id: Long): F[ApiResult[Manufacturer]] =
    selectManufacturersBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getManufacturersBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]] =
    selectManufacturersBy(field, values, operator).asList(Some(field), Some(values)).execute

  override def getManufacturersByCity[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]] =
    selectManufacturersByCity[City, V](field, values, operator)
      .asList(Some(field), Some(values))
      .execute

  override def getManufacturersByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[Manufacturer]]] =
    EitherT(getFieldList[City, Long, Country, V]("id", FieldValues(field, values), operator))
      .flatMapF { cityIds =>
        Nel.fromList(cityIds.value) match {
          case Some(ids) =>
            selectManufacturersByCity[City, Long]("id", ids, Operator.In)
              .asList(Some(field), Some(ids))
          case None => EntryListEmpty.elevate[ConnectionIO, List[Manufacturer]]
        }
      }
      .value
      .execute

  override def createManufacturer(manufacturer: ManufacturerCreate): F[ApiResult[Long]] =
    insertManufacturer(manufacturer).attemptInsert.execute

  override def updateManufacturer(manufacturer: Manufacturer): F[ApiResult[Long]] =
    modifyManufacturer(manufacturer).attemptUpdate(manufacturer.id).execute

  override def partiallyUpdateManufacturer(
    id: Long,
    patch: ManufacturerPatch
  ): F[ApiResult[Manufacturer]] =
    EitherT(getManufacturer(id)).flatMapF { manufacturerOutput =>
      val manufacturer = manufacturerOutput.value
      val patched = Manufacturer.fromPatch(id, patch, manufacturer)
      modifyManufacturer(patched).attemptUpdate(patched).execute
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
