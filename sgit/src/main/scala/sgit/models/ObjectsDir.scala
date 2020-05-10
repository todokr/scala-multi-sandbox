package sgit.models

import java.nio.file.{Files, Path}

object ObjectsDir {
  val DirName: String = "objects"

  def resolve(workTree: Path): Path =
    workTree.resolve(".git").resolve(DirName)

  def init(gitDir: Path): ObjectsDir = {
    val dir = gitDir.resolve(DirName)
    Files.createDirectory(gitDir.resolve(dir))
    ObjectsDir(dir)
  }
}

case class ObjectsDir(dir: Path)
