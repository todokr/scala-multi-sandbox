package sgit.models

import java.nio.file.{Files, Path}

object Refs {
  val DirName: String = "refs"
  val HeadDirName: String = "heads"
  val TagsDirName: String = "tags"

  def init(gitDir: Path): Refs = {
    val dir = gitDir.resolve(DirName)
    val headDir = dir.resolve(HeadDirName)
    val tagsDir = dir.resolve(TagsDirName)
    Files.createDirectory(dir)
    Files.createDirectory(headDir)
    Files.createDirectory(tagsDir)
    Refs(dir)
  }
}

case class Refs(dir: Path)
