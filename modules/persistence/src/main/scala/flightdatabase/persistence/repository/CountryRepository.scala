package flightdatabase.persistence.repository

import cats.data.EitherT
import cats.data.{NonEmptyList => Nel}
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import flightdatabase.ApiResult
import flightdatabase.FieldType
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.country.Country
import flightdatabase.country.CountryAlgebra
import flightdatabase.country.CountryCreate
import flightdatabase.country.CountryPatch
import flightdatabase.currency.Currency
import flightdatabase.language.Language
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.CountryRepository.PartiallyAppliedGetAllCountries
import flightdatabase.persistence.repository.CountryRepository.PartiallyAppliedGetByCountry
import flightdatabase.persistence.repository.CountryRepository.PartiallyAppliedGetByCurrency
import flightdatabase.persistence.repository.CountryRepository.PartiallyAppliedGetByLanguage
import flightdatabase.persistence.repository.queries.CountryQueries._
import flightdatabase.persistence.syntax.all._
import org.typelevel.doobie.Put
import org.typelevel.doobie.Read
import org.typelevel.doobie.Transactor

class CountryRepository[F[_]: Concurrent] private (implicit
  transactor: Transactor[F]
) extends CountryAlgebra[F] {

  override def doesCountryExist(id: Long): F[Boolean] =
    countryExists(id).unique.execute

  override def getCountries: PartiallyAppliedGetAll[F, Country] =
    new PartiallyAppliedGetAllCountries[F]

  override def getCountry(id: Long): F[ApiResult[Country]] =
    selectCountriesBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getCountriesBy: PartiallyAppliedGetBy[F, Country] =
    new PartiallyAppliedGetByCountry[F]

  override def getCountriesByLanguage: PartiallyAppliedGetBy[F, Country] =
    new PartiallyAppliedGetByLanguage[F]

  override def getCountriesByCurrency: PartiallyAppliedGetBy[F, Country] =
    new PartiallyAppliedGetByCurrency[F]

  override def createCountry(country: CountryCreate): F[ApiResult[Long]] =
    insertCountry(country).attemptInsert.execute

  override def updateCountry(country: Country): F[ApiResult[Long]] =
    modifyCountry(country).attemptUpdate(country.id).execute

  override def partiallyUpdateCountry(
    id: Long,
    patch: CountryPatch
  ): F[ApiResult[Country]] =
    EitherT(getCountry(id)).flatMapF { countryOutput =>
      val country = countryOutput.value
      val patched = Country.fromPatch(id, patch, country)
      modifyCountry(patched).attemptUpdate(patched).execute
    }.value

  override def removeCountry(id: Long): F[ApiResult[Unit]] =
    deleteCountry(id).attemptDelete(id).execute
}

object CountryRepository {

  def make[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): F[CountryRepository[F]] =
    new CountryRepository[F].pure[F]

  def resource[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ): Resource[F, CountryRepository[F]] =
    Resource.pure(new CountryRepository[F])

  // The language UNION is assembled as raw fragments and run via `.query[Country]`
  // here, where doobie's auto-derivation isn't imported; derive Read explicitly.
  implicit private val readCountry: Read[Country] = Read.derived

  // Partially applied algebra
  private class PartiallyAppliedGetAllCountries[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Country] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Country]]] =
      selectAllCountries(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Country, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByCountry[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Country] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Country]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectCountriesBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByLanguage[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Country] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Country]]] = {
      implicit val put: Put[V] = fieldType.asPut
      val languageFields =
        Nel.of("main_language_id", "secondary_language_id", "tertiary_language_id")

      selectCountriesByExternal[Language, V](languageFields, field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }

  private class PartiallyAppliedGetByCurrency[F[_]: Concurrent](implicit
    transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Country] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Country]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectCountriesByExternal[Currency, V](
        Nel.one("currency_id"),
        field,
        values,
        operator,
        sortAndLimit
      ).asNel(Some(field), Some(values)).execute
    }
  }
}
