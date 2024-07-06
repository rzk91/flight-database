package flightdatabase.db

import cats.effect.Sync
import com.typesafe.scalalogging.Logger
import doobie.LogHandler
import doobie.util.log.ExecFailure
import doobie.util.log.LogEvent
import doobie.util.log.ProcessingFailure
import doobie.util.log.Success
import org.slf4j.LoggerFactory

class Log4jHandler[F[_]: Sync] private (className: String) extends LogHandler[F] {

  private lazy val logger: Logger = Logger(LoggerFactory.getLogger(className))

  override def run(logEvent: LogEvent): F[Unit] = Sync[F].delay {
    logEvent match {
      case Success(s, a, l, e1, e2) =>
        logger.info(s"""Successful Statement Execution:
               |
               |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
               |
               | arguments = [${a.mkString(", ")}]
               | label     = $l
               |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
              """.stripMargin)

      case ProcessingFailure(s, a, l, e1, e2, t) =>
        logger.whenDebugEnabled {
          logger.error(s"""Failed Resultset Processing:
               |
               |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
               |
               | arguments = [${a.mkString(", ")}]
               | label     = $l
               |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
               |   failure = ${t.getMessage}
              """.stripMargin)
        }

      case ExecFailure(s, a, l, e1, t) =>
        logger.whenDebugEnabled {
          logger.error(s"""Failed Statement Execution:
               |
               |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
               |
               | arguments = [${a.mkString(", ")}]
               | label     = $l
               |   elapsed = ${e1.toMillis.toString} ms exec (failed)
               |   failure = ${t.getMessage}
              """.stripMargin)
        }
    }
  }
}

object Log4jHandler {
  def create[F[_]: Sync](className: String): Log4jHandler[F] = new Log4jHandler[F](className)
}
