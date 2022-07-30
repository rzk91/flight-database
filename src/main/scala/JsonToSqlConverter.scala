import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.parser._
import io.circe.syntax._
import objects.Currency

import java.io._
import java.nio.file.Paths
import scala.io.Source

object JsonToSqlConverter {

  val inputFileName: String = "src/main/resources/sql/currencies/currency_list.json"
  val outputFileName: String = "src/main/resources/sql/currencies/insert_currencies.sql"

  def main(args: Array[String]): Unit = {
    val jsonContent = Source.fromFile(inputFileName)

    val jsonList =
      decode[List[Currency]](jsonContent.getLines().mkString).toOption

    jsonList.foreach(_.map(_.sqlInsert).foreach(println))

    val outputFile = new File(outputFileName)

    val writer = new BufferedWriter(new FileWriter(outputFile))

    jsonList.foreach(_.foreach(e => writer.write(s"${e.sqlInsert}\n")))

    jsonContent.close()
    writer.close()
  }
}
