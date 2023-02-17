package flightdatabase

import JsonToSqlConverter._
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  // TODO: Move these to application.conf
  private val initialise: Boolean = true
  private val clean: Boolean = true

  def main(args: Array[String]): Unit = {
    if (initialise) setupScripts()
    initiateDb(clean)

    logger.info("Successfully completed db setup!")
  }
}
