package flightdatabase.repository.queries

import doobie.Fragment
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.domain.currency.CurrencyModel

private[repository] object CurrencyQueries {

  def selectAllCurrencies: Query0[CurrencyModel] = selectAll.query[CurrencyModel]

  def insertCurrency(model: CurrencyModel): Update0 =
    sql"INSERT INTO currency (name, iso, symbol) VALUES (${model.name}, ${model.iso}, ${model.symbol})".update

  def deleteCurrency(id: Long): Update0 = deleteWhereId[CurrencyModel](id)

  private def selectAll: Fragment =
    fr"SELECT currency.id, currency.name, currency.iso, currency.symbol FROM currency"
}
