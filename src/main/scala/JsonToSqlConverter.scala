import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.generic.extras._

import java.io._

import scala.io.Source
import java.nio.file.Paths

object JsonToSqlConverter {

  def main(args: Array[String]): Unit = {
    val jsonContent = Source.fromFile("src/main/resources/sql/languages/language_list.json")

    val languages = decode[List[Language]](jsonContent.getLines().mkString).toOption

    languages.foreach(_.map(_.sqlInsert).foreach(println))

    val outputFile = new File("src/main/resources/sql/languages/insert_languages.sql")

    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))

    languages.foreach(_.foreach { e =>
      writer.write(s"${e.sqlInsert}\n")
    })

    jsonContent.close()
    writer.close()
  }


}
