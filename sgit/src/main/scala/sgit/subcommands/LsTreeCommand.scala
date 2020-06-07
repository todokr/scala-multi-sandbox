package sgit.subcommands

import java.nio.file.{Files, Path}
import java.util.zip.InflaterInputStream

import sgit.TreeParser

object LsTreeCommand {

  def run(executingPath: Path, hash: String) = {
    val gitDir = executingPath.resolve(".git")
    val (dir, file) = hash.splitAt(2)
    val objectsPath = gitDir.resolve("objects")
    val treePath =
      objectsPath
        .resolve(dir)
        .resolve(file)
        .normalize()
    if (!treePath.startsWith(executingPath)) {
      throw new Exception("Invalid path")
    } else if (!Files.exists(treePath)) {
      throw new Exception(s"no such tree. hash=${hash}")
    }
    val is = new InflaterInputStream(Files.newInputStream(treePath))
    val data = Iterator
      .continually(is.read())
      .dropWhile(_ != 0x00)
      .drop(1) // drop 0x00
      .takeWhile(_ != -1)
      .map(_.toByte)
      .toArray

    TreeParser.parse(data).foreach { tree =>
      println(tree.pretty)
    }
  }
}
