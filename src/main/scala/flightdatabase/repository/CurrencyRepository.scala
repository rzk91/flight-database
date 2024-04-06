package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import flightdatabase.domain.ApiResult
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyModel
import flightdatabase.repository.queries.CurrencyQueries.deleteCurrency
import flightdatabase.repository.queries.CurrencyQueries.insertCurrency
import flightdatabase.repository.queries.CurrencyQueries.selectAllCurrencies
import flightdatabase.utils.implicits._

class CurrencyRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends CurrencyAlgebra[F] {

  override def getCurrencies: F[ApiResult[List[CurrencyModel]]] =
    selectAllCurrencies.asList.execute

  override def getCurrenciesOnlyNames: F[ApiResult[List[String]]] =
    getNameList[CurrencyModel].execute

  override def getCurrency(id: Int): F[ApiResult[CurrencyModel]] =
    featureNotImplemented[F, CurrencyModel]

  override def createCurrency(currency: CurrencyModel): F[ApiResult[Int]] =
    insertCurrency(currency).attemptInsert.execute

  override def updateCurrency(currency: CurrencyModel): F[ApiResult[CurrencyModel]] =
    featureNotImplemented[F, CurrencyModel]

  override def removeCurrency(id: Int): F[ApiResult[Unit]] =
    deleteCurrency(id).attemptDelete.execute
}

object CurrencyRepository {

  def make[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): F[CurrencyRepository[F]] =
    new CurrencyRepository[F].pure[F]

  def resource[F[_]: Concurrent](
    implicit transactor: Resource[F, HikariTransactor[F]]
  ): Resource[F, CurrencyRepository[F]] =
    Resource.pure(new CurrencyRepository[F])
}
