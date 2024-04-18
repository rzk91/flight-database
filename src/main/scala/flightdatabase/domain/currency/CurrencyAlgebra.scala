package flightdatabase.domain.currency

import doobie.Put
import flightdatabase.domain.ApiResult

trait CurrencyAlgebra[F[_]] {
  def doesCurrencyExist(id: Long): F[Boolean]
  def getCurrencies: F[ApiResult[List[Currency]]]
  def getCurrenciesOnlyNames: F[ApiResult[List[String]]]
  def getCurrency(id: Long): F[ApiResult[Currency]]
  def getCurrencies[V: Put](field: String, value: V): F[ApiResult[List[Currency]]]
  def createCurrency(currency: CurrencyCreate): F[ApiResult[Long]]
  def updateCurrency(currency: Currency): F[ApiResult[Long]]
  def partiallyUpdateCurrency(id: Long, patch: CurrencyPatch): F[ApiResult[Currency]]
  def removeCurrency(id: Long): F[ApiResult[Unit]]
}
