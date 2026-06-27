package flightdatabase.currency

import flightdatabase.ApiResult
import flightdatabase.partial.PartiallyAppliedGetAll
import flightdatabase.partial.PartiallyAppliedGetBy

trait CurrencyAlgebra[F[_]] {
  def doesCurrencyExist(id: Long): F[Boolean]
  def getCurrencies: PartiallyAppliedGetAll[F, Currency]
  def getCurrency(id: Long): F[ApiResult[Currency]]
  def getCurrenciesBy: PartiallyAppliedGetBy[F, Currency]
  def createCurrency(currency: CurrencyCreate): F[ApiResult[Long]]
  def updateCurrency(currency: Currency): F[ApiResult[Long]]
  def partiallyUpdateCurrency(id: Long, patch: CurrencyPatch): F[ApiResult[Currency]]
  def removeCurrency(id: Long): F[ApiResult[Unit]]
}
