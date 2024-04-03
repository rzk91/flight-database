package flightdatabase

import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.util.log.ExecFailure
import doobie.util.log.ProcessingFailure
import doobie.util.log.Success

package object db extends LazyLogging {

  // TODO: Move this to `repository` package but after upgrading to doobie 1.0.0-RC5
  // Implicit logging
  implicit val logHandler: LogHandler = LogHandler {
    case Success(s, a, e1, e2) =>
      logger.debug(s"""Successful statement execution:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
          """.stripMargin)

    case ProcessingFailure(s, a, e1, e2, t) =>
      logger.whenDebugEnabled {
        logger.warn(s"""Failed result set processing:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
            |   failure = ${t.getMessage}
          """.stripMargin)
      }

    case ExecFailure(s, a, e1, t) =>
      logger.whenDebugEnabled {
        logger.error(s"""Failed statement execution:
            |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
            |
            | arguments = [${a.mkString(", ")}]
            |   elapsed = ${e1.toMillis.toString} ms exec (failed)
            |   failure = ${t.getMessage}
          """.stripMargin)
      }
  }
}
