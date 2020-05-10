package sgit.subcommands

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.zip.InflaterInputStream

import scala.util.chaining._

import sgit.models.GitObject

object CatFileCommand {

  def run(showType: Boolean,
          prettyPrint: Boolean,
          hash: String,
          executingPath: Path): Unit = {
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

    if (showType) {
      println(gitObject.typeName)
    } else if (prettyPrint) {
      val content =
        gitObject.serialize.pipe(new String(_, StandardCharsets.UTF_8))
      println(content)
    }

    is.close()
  }
}
