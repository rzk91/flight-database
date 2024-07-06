package flightdatabase.utils.extensions

import flightdatabase.utils.extensions.path._
import org.apache.commons.io.FilenameUtils

import java.io.File
import java.nio.file.Path

final class PathOps(val path: Path) extends AnyVal {
  def fileName: String = path.getFileName.toString
  def folderName: String = path.getParent.fileName
  def fileStartsWith(prefix: String): Boolean = fileName.startsWith(prefix)
  def fileEndsWith(suffix: String): Boolean = fileName.endsWith(suffix)
  def baseName: String = FilenameUtils.getBaseName(fileName)
  def extension: String = FilenameUtils.getExtension(fileName)
  def jsonFile: Boolean = FilenameUtils.isExtension(fileName, "json")
  def sqlFile: Boolean = FilenameUtils.isExtension(fileName, "sql")
  def directory: Boolean = path.toFile.isDirectory
  def mkdirs: Boolean = path.toFile.getParentFile.mkdirs()
  def absolutePath: String = path.toFile.getAbsolutePath

  def listContents(filterFunc: File => Boolean = _ => true): List[File] =
    path.toFile.listFiles.filter(filterFunc).toList
}

trait ToPathOps {
  @inline implicit def enrichPath(path: Path): PathOps = new PathOps(path)
}

object path extends ToPathOps
