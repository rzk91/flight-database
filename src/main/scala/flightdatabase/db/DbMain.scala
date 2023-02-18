package flightdatabase.db

import flightdatabase.config.Configuration.setupConfig.createScripts
import flightdatabase.db.JsonToSqlConverter._
import com.typesafe.scalalogging.LazyLogging

object DbMain extends LazyLogging {

  def main(args: Array[String]): Unit = {
    if (createScripts) setupScripts()
    initiateDb()

    logger.info("Successfully completed db setup!")
  }
}
