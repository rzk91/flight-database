package flightdatabase.repository

import cats.data.EitherT
import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.Put
import doobie.Transactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyCreate
import flightdatabase.domain.currency.CurrencyPatch
import flightdatabase.repository.queries.CurrencyQueries._
import flightdatabase.utils.implicits._

class CurrencyRepository[F[_]: Concurrent] private (
  implicit transactor: Transactor[F]
) extends CurrencyAlgebra[F] {

  override def doesCurrencyExist(id: Long): F[Boolean] = currencyExists(id).unique.execute

  override def getCurrencies: F[ApiResult[List[Currency]]] =
    selectAllCurrencies.asList.execute

  override def getCurrenciesOnlyNames: F[ApiResult[List[String]]] =
    getFieldList[Currency, String]("name").execute

  override def getCurrency(id: Long): F[ApiResult[Currency]] =
    featureNotImplemented[F, Currency]

  override def getCurrencies[V: Put](field: String, value: V): F[ApiResult[List[Currency]]] =
    selectCurrencyBy(field, value).asList.execute

  override def createCurrency(currency: CurrencyCreate): F[ApiResult[Long]] =
    insertCurrency(currency).attemptInsert.execute

  override def updateCurrency(currency: Currency): F[ApiResult[Currency]] =
    modifyCurrency(currency).attemptUpdate(currency).execute

  override def partiallyUpdateCurrency(id: Long, patch: CurrencyPatch): F[ApiResult[Currency]] =
    EitherT(getCurrency(id)).flatMapF { currencyOutput =>
      val currency = currencyOutput.value
      updateCurrency(Currency.fromPatch(id, patch, currency))
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
}
