package flightdatabase.repository.queries

import cats.data.{NonEmptyList => Nel}
import doobie.Fragment
import doobie.Put
import doobie.Query0
import doobie.Update0
import doobie.implicits._
import flightdatabase.api.Operator
import flightdatabase.domain.currency.Currency
import flightdatabase.domain.currency.CurrencyCreate

private[repository] object CurrencyQueries {

  def currencyExists(id: Long): Query0[Boolean] = idExistsQuery[Currency](id)

  def selectAllCurrencies: Query0[Currency] = selectAll.query[Currency]

  def selectCurrencyBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator
  ): Query0[Currency] =
    (selectAll ++ whereFragment(s"currency.$field", values, operator)).query[Currency]

  def insertCurrency(model: CurrencyCreate): Update0 =
    sql"""INSERT INTO currency
         |       (name, iso, symbol)
         |   VALUES (
         |       ${model.name},
         |       ${model.iso},
         |       ${model.symbol}
         |   )
         | """.stripMargin.update

  def modifyCurrency(model: Currency): Update0 =
    sql"""
         | UPDATE currency
         | SET
         |  name = ${model.name},
         |  iso = ${model.iso},
         |  symbol = ${model.symbol}
         | WHERE id = ${model.id}
       """.stripMargin.update

  def deleteCurrency(id: Long): Update0 = deleteWhereId[Currency](id)

  private def selectAll: Fragment =
    fr"SELECT currency.id, currency.name, currency.iso, currency.symbol FROM currency"
}
