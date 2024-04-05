package flightdatabase.repository.queries

import doobie.{Query0, Update0}
import doobie.implicits._
import flightdatabase.domain.currency.CurrencyModel

private[repository] object CurrencyQueries {

  def selectAllCurrencies: Query0[CurrencyModel] = selectAllQuery[CurrencyModel]

  def insertCurrency(model: CurrencyModel): Update0 =
    sql"INSERT INTO currency (name, iso, symbol) VALUES (${model.name}, ${model.iso}, ${model.symbol})".update

  def deleteCurrency(id: Int): Update0 = deleteWhereId[CurrencyModel](id)
}
