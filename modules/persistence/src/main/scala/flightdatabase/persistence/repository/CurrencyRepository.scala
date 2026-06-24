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
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyAlgebra
import flightdatabase.currency.CurrencyCreate
import flightdatabase.currency.CurrencyPatch
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy
import flightdatabase.persistence.repository.CurrencyRepository.PartiallyAppliedGetAllCurrencies
import flightdatabase.persistence.repository.CurrencyRepository.PartiallyAppliedGetByCurrency
import flightdatabase.persistence.repository.queries.CurrencyQueries._
import flightdatabase.persistence.syntax.all._

class CurrencyRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CurrencyAlgebra[F] {

  override def doesCurrencyExist(id: Long): F[Boolean] = currencyExists(id).unique.execute

  override def getCurrencies: PartiallyAppliedGetAll[F, Currency] =
    new PartiallyAppliedGetAllCurrencies[F]

  override def getCurrency(id: Long): F[ApiResult[Currency]] =
    selectCurrencyBy("id", Nel.one(id), Operator.Equals, ValidatedSortAndLimit.empty)
      .asSingle(id)
      .execute

  override def getCurrenciesBy: PartiallyAppliedGetBy[F, Currency] =
    new PartiallyAppliedGetByCurrency[F]

  override def createCurrency(currency: CurrencyCreate): F[ApiResult[Long]] =
    insertCurrency(currency).attemptInsert.execute

  override def updateCurrency(currency: Currency): F[ApiResult[Long]] =
    modifyCurrency(currency).attemptUpdate(currency.id).execute

  override def partiallyUpdateCurrency(id: Long, patch: CurrencyPatch): F[ApiResult[Currency]] =
    EitherT(getCurrency(id)).flatMapF { currencyOutput =>
      val currency = currencyOutput.value
      val patched = Currency.fromPatch(id, patch, currency)
      modifyCurrency(patched).attemptUpdate(patched).execute
    }.value

  override def removeCurrency(id: Long): F[ApiResult[Unit]] =
    deleteCurrency(id).attemptDelete(id).execute
}

object CurrencyRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): F[CurrencyRepository[F]] =
    new CurrencyRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ): Resource[F, CurrencyRepository[F]] =
    Resource.pure(new CurrencyRepository[F])

  // Partially applied algebra
  private class PartiallyAppliedGetAllCurrencies[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetAll[F, Currency] {

    override def apply(sortAndLimit: ValidatedSortAndLimit): F[ApiResult[Nel[Currency]]] =
      selectAllCurrencies(sortAndLimit).asNel().execute

    override def apply[V](
      sortAndLimit: ValidatedSortAndLimit,
      returnField: String,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[V]]] = {
      implicit val read: Read[V] = fieldType.asRead
      getFieldList[Currency, V](sortAndLimit, returnField).execute
    }
  }

  private class PartiallyAppliedGetByCurrency[F[_]: Concurrent](
    implicit transactor: Transactor[F]
  ) extends PartiallyAppliedGetBy[F, Currency] {

    override def apply[V](
      field: String,
      values: Nel[V],
      operator: Operator,
      sortAndLimit: ValidatedSortAndLimit,
      fieldType: FieldType[V]
    ): F[ApiResult[Nel[Currency]]] = {
      implicit val put: Put[V] = fieldType.asPut
      selectCurrencyBy(field, values, operator, sortAndLimit)
        .asNel(Some(field), Some(values))
        .execute
    }
  }
}
