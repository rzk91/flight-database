import io.circe._
import io.circe.parser._
import scala.io.Source

object JsonToSqlConverter {
  
  def main(args: Array[String]): Unit = {
    val jsonContent = Source.fromFile("src/main/resources/sql/languages/language_list.json")

    val languages = jsonContent.getLines.map(parse)

    languages.foreach(println)

    jsonContent.close()
  }


}
