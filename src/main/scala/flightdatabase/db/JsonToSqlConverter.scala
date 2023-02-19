package flightdatabase.db

import com.typesafe.scalalogging.LazyLogging
import flightdatabase.db.objects._
import flightdatabase.utils.CollectionsHelper._
import flightdatabase.utils.FileHelper._
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.parser._
import io.circe.syntax._

import java.io._
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.CollectionConverters._

object JsonToSqlConverter extends LazyLogging {

  private val resourcePath: String = "src/main/resources/db"

  private val relevantDbObjects: List[String] =
    List(
      "languages",
      "currencies",
      "countries",
      "cities",
      "manufacturers",
      "airports",
      "fleets",
      "airplanes",
      "fleet_airplanes",
      "fleet_routes"
    )

  def setupScripts(): Unit = {
    val jsonFiles = relevantFiles(relevantDbObjects)

    jsonFiles.zipWithIndex
      .flatMap {
        case (p, idx) =>
          val dbObject = p.baseName
          val versionIdx = idx + 3 // V1 and V2 are for init scripts
          val content = {
            val j = Source.fromFile(p.absolutePath)
            val out = j.getLines().mkString
            j.close()
            out
          }
          val jsons: Option[List[DbObject]] = dbObject match {
            case "airplanes"       => decode[List[Airplane]](content).toOptionWithDebug
            case "airports"        => decode[List[Airport]](content).toOptionWithDebug
            case "cities"          => decode[List[City]](content).toOptionWithDebug
            case "countries"       => decode[List[Country]](content).toOptionWithDebug
            case "currencies"      => decode[List[Currency]](content).toOptionWithDebug
            case "fleets"          => decode[List[Fleet]](content).toOptionWithDebug
            case "languages"       => decode[List[Language]](content).toOptionWithDebug
            case "manufacturers"   => decode[List[Manufacturer]](content).toOptionWithDebug
            case "fleet_airplanes" => decode[List[FleetAirplane]](content).toOptionWithDebug
            case "fleet_routes"    => decode[List[FleetRoute]](content).toOptionWithDebug
            case _                 => None
          }

          jsons.map((versionIdx, dbObject, _))
      }
      .foreach {
        case (versionIdx, dbObject, jsonList) =>
          logger.debug(
            s"[$dbObject] Converting to SQL insert statements: $jsonList"
          )
          val writer = writerFor(outputFile(versionIdx, dbObject))
          jsonList.foreach(e => writer.write(s"${e.sqlInsert}\n"))
          writer.close()
      }

    logger.info(s"Successfully created ${jsonFiles.length} SQL scripts!")
  }

  def relevantFiles(dbObjects: List[String]): List[Path] = {
    val files = Files
      .walk(Paths.get(s"$resourcePath/json/"))
      .filter(_.jsonFile)
      .iterator
      .asScala
      .toList

    val filesWithFolder = files.map(_.baseName).zip(files).toMap

    dbObjects.flatMap(filesWithFolder.get)
  }

  def outputFile(idx: Int, dbObject: String): String =
    s"$resourcePath/migration/V${idx}__insert_$dbObject.sql"

  def writerFor(file: String): BufferedWriter =
    new BufferedWriter(new FileWriter(new File(file)))
}
