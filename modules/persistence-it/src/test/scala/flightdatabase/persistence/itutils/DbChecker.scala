package flightdatabase.persistence.itutils

import cats.effect.Async
import cats.effect.IO
import org.typelevel.doobie.scalatest.IOChecker
import org.scalatest.matchers.should.Matchers

trait DbChecker extends PostgreSqlContainerSpec[IO] with Matchers with IOChecker {
  implicit override val async: Async[IO] = IO.asyncForIO
}
