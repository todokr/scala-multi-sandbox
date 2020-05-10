package sgit.subcommands

import java.nio.file.{Files, Path}
import java.util.zip.InflaterInputStream

import sgit.models.GitObject

object CatFileCommand {

  def run(objectType: String, hash: String, executingPath: Path): Unit = {
    val (dir, file) = hash.splitAt(2)
    val objectPath = executingPath
      .resolve(".git/objects")
      .resolve(dir)
      .resolve(file)
      .normalize()
    if (!objectPath.startsWith(executingPath)) {
      throw new Exception("Invalid object")
    }
    val is = new InflaterInputStream(Files.newInputStream(objectPath))

    val data = Iterator.continually(is.read()).map(_.toByte)
    val gitObject = GitObject.deserialize(data)
    println(gitObject)
    println(s"type: $objectType")
    is.close()
  }
}
