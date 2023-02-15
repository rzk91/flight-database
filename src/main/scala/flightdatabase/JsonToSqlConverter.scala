package flightdatabase

import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.parser._
import io.circe.syntax._
import flightdatabase.objects._
import flightdatabase.utils.FileHelper._
import flightdatabase.utils.CollectionsHelper._

import java.io._
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters._
import com.typesafe.scalalogging.LazyLogging

object JsonToSqlConverter extends LazyLogging {

  private val resourceSqlPath: String = "src/main/resources/sql/"

  private val relevantFolders: Set[String] =
    Set(
      "airplanes",
      "airports",
      "cities",
      "countries",
      "currencies",
      "fleets",
      "languages",
      "manufacturers"
    )

  def main(args: Array[String]): Unit = {
    val jsonFiles = relevantFiles(relevantFolders)

    jsonFiles
      .flatMap { p =>
        val folder = p.folderName
        val content = {
          val j = Source.fromFile(p.absolutePath)
          val out = j.getLines().mkString
          j.close()
          out
        }
        val jsons: Option[List[DbObject]] = folder match {
          case "airplanes"  => decode[List[Airplane]](content).toOptionWithDebug
          case "airports"   => decode[List[Airport]](content).toOptionWithDebug
          case "cities"     => decode[List[City]](content).toOptionWithDebug
          case "countries"  => decode[List[Country]](content).toOptionWithDebug
          case "currencies" => decode[List[Currency]](content).toOptionWithDebug
          case "fleets"     => decode[List[Fleet]](content).toOptionWithDebug
          case "languages"  => decode[List[Language]](content).toOptionWithDebug
          case "manufacturers" =>
            decode[List[Manufacturer]](content).toOptionWithDebug
          case _ => None
        }

        jsons.map((folder, _))
      }
      .foreach {
        case (folder, jsonList) =>
          logger.debug(
            s"[$folder] Converting to SQL insert statements: $jsonList"
          )
          val outFile = new File(outputFile(folder))
          val writer = new BufferedWriter(new FileWriter(outFile))
          jsonList.foreach(e => writer.write(s"${e.sqlInsert}\n"))
          writer.close()
      }

    logger.info(s"Successfully created ${jsonFiles.length} SQL scripts!")
  }

  def relevantFiles(folders: Set[String]): List[Path] = {
    val files = Files
      .walk(Paths.get(resourceSqlPath))
      .filter(p => folders(p.folderName))
      .filter(_.jsonFile)

    files.iterator.asScala.toList
  }

  def inputFile(folder: String): String =
    s"$resourceSqlPath$folder/$folder.json"

  def outputFile(folder: String): String =
    s"$resourceSqlPath$folder/insert_$folder.sql"
}
