package sgit.models

import java.nio.file.{Files, Path}

import org.ini4j.Wini

case class Config(repositoryFormatVersion: Int) {
  def isSupportedVersion: Boolean = repositoryFormatVersion == 0

  def write(path: Path): Unit = {
    val ini = new Wini(path.toFile)
    ini.put("core", "repositoryFormatVersion", 0)
    ini.store()
  }
}

object Config {
  val default: Config = Config(repositoryFormatVersion = 0)

  def init(gitDir: Path): Config = {
    val configPath = gitDir.resolve("config")
    Files.createFile(configPath)
    default.write(configPath)
    default
  }
}
