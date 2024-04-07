package flightdatabase.domain.currency

import flightdatabase.domain.ApiResult

trait CurrencyAlgebra[F[_]] {
  def getCurrencies: F[ApiResult[List[CurrencyModel]]]
  def getCurrenciesOnlyNames: F[ApiResult[List[String]]]
  def getCurrency(id: Long): F[ApiResult[CurrencyModel]]
  def createCurrency(currency: CurrencyModel): F[ApiResult[Long]]
  def updateCurrency(currency: CurrencyModel): F[ApiResult[CurrencyModel]]
  def removeCurrency(id: Long): F[ApiResult[Unit]]
}
