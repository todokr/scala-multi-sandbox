package sgit.models

import java.nio.file.{Files, Path}

object Objects {
  val DirName: String = "objects"

  def init(gitDir: Path): Objects = {
    val dir = gitDir.resolve(DirName)
    Files.createDirectory(gitDir.resolve(dir))
    Objects(dir)
  }
}

case class Objects(dir: Path)
