package sgit.models

import java.nio.file.{Files, Path}

object BranchesDir {
  val DirName: String = "branches"

  def init(gitDir: Path): BranchesDir = {
    val dir = gitDir.resolve(DirName)
    Files.createDirectory(dir)
    BranchesDir(dir)
  }
}

case class BranchesDir(dir: Path)
