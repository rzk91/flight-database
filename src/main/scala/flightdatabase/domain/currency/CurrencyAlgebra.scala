package flightdatabase.domain.currency

import flightdatabase.domain.ApiResult

trait CurrencyAlgebra[F[_]] {
  def getCurrencies: F[ApiResult[List[CurrencyModel]]]
  def getCurrenciesOnlyNames: F[ApiResult[List[String]]]
  def getCurrency(id: Int): F[ApiResult[CurrencyModel]]
  def createCurrency(currency: CurrencyModel): F[ApiResult[Int]]
  def updateCurrency(currency: CurrencyModel): F[ApiResult[CurrencyModel]]
  def removeCurrency(id: Int): F[ApiResult[Unit]]
}
