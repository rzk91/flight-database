package flightdatabase.repository

import cats.effect.Concurrent
import cats.effect.Resource
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import flightdatabase.domain.ApiResult
import flightdatabase.domain.FlightDbTable.CURRENCY
import flightdatabase.domain.currency.CurrencyAlgebra
import flightdatabase.domain.currency.CurrencyModel
import flightdatabase.utils.implicits._

class CurrencyRepository[F[_]: Concurrent] private (
  implicit transactor: Resource[F, HikariTransactor[F]]
) extends CurrencyAlgebra[F] {

  override def getCurrencies: F[ApiResult[List[CurrencyModel]]] =
    sql"SELECT id, name, iso, symbol FROM currency"
      .query[CurrencyModel]
      .to[List]
      .map(liftListToApiResult)
      .execute

  override def getCurrenciesOnlyNames: F[ApiResult[List[String]]] =
    getStringList(CURRENCY).execute

  override def getCurrencyById(id: Long): F[ApiResult[CurrencyModel]] =
    featureNotImplemented[F, CurrencyModel]

  override def createCurrency(currency: CurrencyModel): F[ApiResult[Long]] =
    featureNotImplemented[F, Long]

  override def updateCurrency(currency: CurrencyModel): F[ApiResult[CurrencyModel]] =
    featureNotImplemented[F, CurrencyModel]

  override def deleteCurrency(id: Long): F[ApiResult[CurrencyModel]] =
    featureNotImplemented[F, CurrencyModel]
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
