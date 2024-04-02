package flightdatabase.domain.currency

trait CurrencyAlgebra[F[_]] {
  def getCurrencies: F[List[CurrencyModel]]
  def getCurrenciesOnlyNames: F[List[String]]
  def getCurrencyById(id: Long): F[Option[CurrencyModel]]
  def createCurrency(currency: CurrencyModel): F[Long]
  def updateCurrency(currency: CurrencyModel): F[Option[CurrencyModel]]
  def deleteCurrency(id: Long): F[Option[CurrencyModel]]
}
