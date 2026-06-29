package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import com.ibm.icu.util.TimeZone
import flightdatabase.ApiError
import flightdatabase.ApiOutput
import flightdatabase.ApiResult
import flightdatabase.EntryHasInvalidForeignKey
import flightdatabase.EntryNotFound
import flightdatabase.FieldType
import flightdatabase.Got
import flightdatabase.InvalidTimezone
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.city.City
import flightdatabase.city.CityAlgebra
import flightdatabase.city.CityCreate
import flightdatabase.city.CityPatch
import flightdatabase.country.Country
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.CityRepository.PartiallyAppliedGetAllCities
import flightdatabase.persistence.repository.CityRepository.PartiallyAppliedGetByCity
import flightdatabase.persistence.repository.CityRepository.PartiallyAppliedGetByCountry
import flightdatabase.persistence.repository.queries.CityQueries._
import flightdatabase.persistence.repository.queries.CountryQueries
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class CityRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CityAlgebra[F] {

  override def doesCityExist(id: Long): F[Boolean] = cityExists(id).unique.execute

  override def getCities: PartiallyAppliedGetAll[F, City] =
    new PartiallyAppliedGetAllCities[F]

  override def getCity(id: Long): F[ApiResult[City]] =
    selectCitiesBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getCitiesBy: PartiallyAppliedGetBy[F, City] =
    new PartiallyAppliedGetByCity[F]

  override def getCitiesByCountry: PartiallyAppliedGetBy[F, City] =
    new PartiallyAppliedGetByCountry[F]

  // Do we need checks for latitude/longitude <-> country matching?
  override def createCity(city: CityCreate): F[ApiResult[Long]] =
    validateTimezone(city.timezone, city.countryId).flatMap {
      case Left(error) => error.elevate[F, Long]
      case Right(_)    => insertCity(city).attemptInsert.execute
    }

  override def updateCity(city: City): F[ApiResult[Long]] =
    validateTimezone(city.timezone, city.countryId).flatMap {
      case Left(error) => error.elevate[F, Long]
      case Right(_)    => modifyCity(city).attemptUpdate(city.id).execute
    }

  override def partiallyUpdateCity(id: Long, patch: CityPatch): F[ApiResult[City]] =
    EitherT(getCity(id)).flatMapF { cityOutput =>
      val city = cityOutput.value
      val patched = City.fromPatch(id, patch, city)
      validateTimezone(patched.timezone, patched.countryId).flatMap {
        case Left(error) => error.elevate[F, City]
        case Right(_)    => modifyCity(patched).attemptUpdate(patched).execute
      }
    }.value

  override def removeCity(id: Long): F[ApiResult[Unit]] =
    deleteCity(id).attemptDelete(id).execute

  override protected def validateTimezone(timezone: String, countryId: Long): F[ApiResult[Unit]] =
    EitherT(
      CountryQueries
        .selectCountriesBy("id", Nel.one(countryId), Operator.Equals, ValidatedSortAndLimit.empty)
        .map(_.iso2)
        .asSingle(countryId)
    ).leftMap {
        case EntryNotFound(_) => EntryHasInvalidForeignKey
        case other            => other
      }
      .subflatMap[ApiError, ApiOutput[Unit]] { countryIso2Output =>
        val countryIso2 = countryIso2Output.value
        Either
          .raiseUnless(timezoneMatchesCountry(timezone, countryIso2))(InvalidTimezone(timezone))
          .map(Got(_))
      }
      .value
      .execute

  private def timezoneMatchesCountry(tz: String, country: String): Boolean =
    TimeZone.getAvailableIDs(country).contains(tz)
}

object CityRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[CityRepository[F]] = new CityRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, CityRepository[F]] = Resource.pure(new CityRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllCities[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, City] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[City]]] =
      selectAllCities(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[City, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByCity[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, City] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[City]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectCitiesBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCountry[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, City] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[City]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectCitiesByExternal[Country, V](field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
