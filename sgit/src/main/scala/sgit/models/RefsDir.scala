package sgit.models

import java.nio.file.{Files, Path}

object RefsDir {
  val DirName: String = "refs"
  val HeadDirName: String = "heads"
  val TagsDirName: String = "tags"

  def init(gitDir: Path): RefsDir = {
    val dir = gitDir.resolve(DirName)
    val headDir = dir.resolve(HeadDirName)
    val tagsDir = dir.resolve(TagsDirName)
    Files.createDirectory(dir)
    Files.createDirectory(headDir)
    Files.createDirectory(tagsDir)
    RefsDir(dir)
  }
}

case class RefsDir(dir: Path)
