package flightdatabase.persistence.repository.queries

import cats.data.{NonEmptyList => Nel}
import org.typelevel.doobie.Fragment
import org.typelevel.doobie.Put
import org.typelevel.doobie.Query0
import org.typelevel.doobie.Update0
import org.typelevel.doobie.implicits._
import flightdatabase.Operator
import flightdatabase.ValidatedSortAndLimit
import flightdatabase.currency.Currency
import flightdatabase.currency.CurrencyCreate
import flightdatabase.persistence.syntax.sortandlimit._

private[repository] object CurrencyQueries {

  def currencyExists(id: Long): Query0[Boolean] = idExistsQuery[Currency](id)

  def selectAllCurrencies(sortAndLimit: ValidatedSortAndLimit): Query0[Currency] =
    (selectAll ++ sortAndLimit.fragment).query[Currency]

  def selectCurrencyBy[V: Put](
    field: String,
    values: Nel[V],
    operator: Operator,
    sortAndLimit: ValidatedSortAndLimit
  ): Query0[Currency] =
    (selectAll ++ whereFragment(s"currency.$field", values, operator) ++ sortAndLimit.fragment)
      .query[Currency]

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
