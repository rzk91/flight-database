package flightdatabase.syntax

import flightdatabase.syntax.path._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files
import java.nio.file.Paths

final class PathOpsSpec extends AnyFlatSpec with Matchers {

  "fileName / folderName" should "extract the file and its parent folder" in {
    val p = Paths.get("/home/user/docs/report.json")
    p.fileName shouldBe "report.json"
    p.folderName shouldBe "docs"
  }

  "fileStartsWith / fileEndsWith" should "match against the file name" in {
    val p = Paths.get("/tmp/data/seed_v2.sql")
    p.fileStartsWith("seed") shouldBe true
    p.fileStartsWith("data") shouldBe false
    p.fileEndsWith(".sql") shouldBe true
    p.fileEndsWith(".json") shouldBe false
  }

  "baseName / extension" should "split the file name around the last dot" in {
    val p = Paths.get("/a/b/archive.tar.gz")
    p.baseName shouldBe "archive.tar"
    p.extension shouldBe "gz"
  }

  "jsonFile / sqlFile" should "detect the known extensions" in {
    Paths.get("seed.json").jsonFile shouldBe true
    Paths.get("seed.sql").jsonFile shouldBe false
    Paths.get("seed.sql").sqlFile shouldBe true
    Paths.get("seed.json").sqlFile shouldBe false
  }

  "directory / listContents" should "reflect the real filesystem" in {
    val dir = Files.createTempDirectory("pathops-spec")
    dir.toFile.deleteOnExit()
    val json = Files.createFile(dir.resolve("a.json"))
    val sql = Files.createFile(dir.resolve("b.sql"))
    json.toFile.deleteOnExit()
    sql.toFile.deleteOnExit()

    dir.directory shouldBe true
    json.directory shouldBe false

    dir.listContents().map(_.getName).toSet shouldBe Set("a.json", "b.sql")
    dir.listContents(_.getName.endsWith(".json")).map(_.getName) shouldBe List("a.json")
  }

  "absolutePath" should "return an absolute path string" in {
    val p = Paths.get("relative/file.txt")
    p.absolutePath should endWith("file.txt")
    new java.io.File(p.absolutePath).isAbsolute shouldBe true
  }
}
