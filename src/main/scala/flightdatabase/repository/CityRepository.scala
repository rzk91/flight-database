package flightdatabase.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import com.ibm.icu.util.TimeZone
import doobie.Put
import doobie.Transactor
import flightdatabase.api.Operator
import flightdatabase.domain._
import flightdatabase.domain.city.City
import flightdatabase.domain.city.CityAlgebra
import flightdatabase.domain.city.CityCreate
import flightdatabase.domain.city.CityPatch
import flightdatabase.domain.country.Country
import flightdatabase.repository.queries.CityQueries._
import flightdatabase.repository.queries.CountryQueries
import flightdatabase.utils.implicits._

class CityRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CityAlgebra[F] {

  override def doesCityExist(id: Long): F[Boolean] = cityExists(id).unique.execute

  override def getCities: F[ApiResult[List[City]]] = selectAllCities.asList().execute

  override def getCitiesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[City, String]("name").execute

  override def getCity(id: Long): F[ApiResult[City]] =
    selectCitiesBy("id", Nel.one(id), Operator.Equals).asSingle(id).execute

  override def getCitiesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[City]]] =
    selectCitiesBy(field, values, operator).asList(Some(field), Some(values)).execute

  def getCitiesByCountry[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[List[City]]] =
    selectCitiesByExternal[Country, V](field, values, operator)
      .asList(Some(field), Some(values))
      .execute

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
        .selectCountriesBy("id", Nel.one(countryId), Operator.Equals)
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
}
