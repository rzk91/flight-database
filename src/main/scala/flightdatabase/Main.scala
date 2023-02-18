package flightdatabase

import flightdatabase.config.Configuration.setupConfig.createScripts
import JsonToSqlConverter._
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    if (createScripts) setupScripts()
    initiateDb()

    logger.info("Successfully completed db setup!")
  }
}
