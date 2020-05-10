package sgit

import java.nio.file.Files.isRegularFile
import java.nio.file.Path

import org.ini4j.{Ini, IniPreferences}
import sgit.models.Config

object ConfigParser {

  def parse(confPath: Path): Config = {
    if (isRegularFile(confPath)) {
      val pref = new IniPreferences(new Ini(confPath.toFile))
      val coreNode = pref.node("core")
      if (!coreNode.nodeExists("repositoryformatversion")) {}
      val version = coreNode.getInt("repositoryformatversion", -1)
      Config(repositoryFormatVersion = version)

    } else throw new Exception(s"Config file doesn't exist. path:$confPath")
  }
}
