import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.parser._
import io.circe.syntax._
import objects._
import utils.FileHelper._

import java.io._
import java.nio.file.{Files, Path, Paths}
import scala.io.Source
import scala.jdk.CollectionConverters._

object JsonToSqlConverter {

  val inputFileName: String =
    "src/main/resources/sql/countries/country_list.json"

  val outputFileName: String =
    "src/main/resources/sql/countries/insert_countries.sql"

  def relevantFiles: List[Path] = {
    val files = Files
      .walk(Paths.get("src/main/resources/sql/"))
      .filter(f => f.jsonFile || f.sqlFile)

    files.iterator.asScala.toList
  }

  def main(args: Array[String]): Unit = {
    println(relevantFiles)

    val jsonContent = Source.fromFile(inputFileName)

    val jsonList =
      decode[List[Country]](jsonContent.getLines().mkString)

    jsonList.foreach(_.map(_.sqlInsert).foreach(println))

    // val outputFile = new File(outputFileName)

    // val writer = new BufferedWriter(new FileWriter(outputFile))

    // jsonList.foreach(_.foreach(e => writer.write(s"${e.sqlInsert}\n")))

    jsonContent.close()
    // writer.close()
  }
}
