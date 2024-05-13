package flightdatabase.domain.currency

import cats.data.{NonEmptyList => Nel}
import doobie.Put
import flightdatabase.api.Operator
import flightdatabase.domain.ApiResult

trait CurrencyAlgebra[F[_]] {
  def doesCurrencyExist(id: Long): F[Boolean]
  def getCurrencies: F[ApiResult[Nel[Currency]]]
  def getCurrenciesOnlyNames: F[ApiResult[Nel[String]]]
  def getCurrency(id: Long): F[ApiResult[Currency]]

  def getCurrenciesBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): F[ApiResult[Nel[Currency]]]

  def createCurrency(currency: CurrencyCreate): F[ApiResult[Long]]
  def updateCurrency(currency: Currency): F[ApiResult[Long]]
  def partiallyUpdateCurrency(id: Long, patch: CurrencyPatch): F[ApiResult[Currency]]
  def removeCurrency(id: Long): F[ApiResult[Unit]]
}
