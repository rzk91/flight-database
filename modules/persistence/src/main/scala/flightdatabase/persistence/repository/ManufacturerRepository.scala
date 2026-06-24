package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Read
import doobie.Transactor
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.FieldValues
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.city.City
import flightdatabase.country.Country
import flightdatabase.manufacturer.Manufacturer
import flightdatabase.manufacturer.ManufacturerAlgebra
import flightdatabase.manufacturer.ManufacturerCreate
import flightdatabase.manufacturer.ManufacturerPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.ManufacturerRepository.PartiallyAppliedGetAllManufacturers
import flightdatabase.persistence.repository.ManufacturerRepository.PartiallyAppliedGetByCity
import flightdatabase.persistence.repository.ManufacturerRepository.PartiallyAppliedGetByCountry
import flightdatabase.persistence.repository.ManufacturerRepository.PartiallyAppliedGetByManufacturer
import flightdatabase.persistence.repository.queries.ManufacturerQueries._
import flightdatabase.persistence.syntax.all._

class ManufacturerRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends ManufacturerAlgebra[F] {

  override def doesManufacturerExist(id: Long): F[Boolean] =
    manufacturerExists(id).unique.execute

  override def getManufacturers: PartiallyAppliedGetAll[F, Manufacturer] =
    new PartiallyAppliedGetAllManufacturers[F]

  override def getManufacturer(id: Long): F[ApiResult[Manufacturer]] =
    selectManufacturersBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getManufacturersBy: PartiallyAppliedGetBy[F, Manufacturer] =
    new PartiallyAppliedGetByManufacturer[F]

  override def getManufacturersByCity: PartiallyAppliedGetBy[F, Manufacturer] =
    new PartiallyAppliedGetByCity[F]

  override def getManufacturersByCountry: PartiallyAppliedGetBy[F, Manufacturer] =
    new PartiallyAppliedGetByCountry[F]

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

  // Partially applied algebra
  private class PartiallyAppliedGetAllManufacturers[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Manufacturer] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Manufacturer]]] =
      selectAllManufacturers(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Manufacturer, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByManufacturer[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Manufacturer] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Manufacturer]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectManufacturersBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCity[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Manufacturer] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Manufacturer]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectManufacturersByCity[City, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCountry[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Manufacturer] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Manufacturer]]] = {
      implicit val put: Put[V] = fieldType.asPut
      EitherT(getFieldList[City, Long, Country, V]("id", FieldValues(field, values), operator))
        .flatMapF { cityIds =>
          val ids = cityIds.value
          selectManufacturersByCity[City, Long]("id", ids, Operator.In, sortAndLimit)
            .asNel(Some(field), Some(ids))
        }
        .value
        .execute
    }
  }
}
