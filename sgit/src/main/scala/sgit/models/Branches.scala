package sgit.models

import java.nio.file.{Files, Path}

object Branches {
  val DirName: String = "branches"

  def init(gitDir: Path): Branches = {
    val dir = gitDir.resolve(DirName)
    Files.createDirectory(dir)
    Branches(dir)
  }
}

case class Branches(dir: Path)
