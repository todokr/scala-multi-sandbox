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
      val invalidMessages = Seq(
        coreNode.nodeExists("repositoryformatversion") -> "Not exist repositoryformatversion"
      ).collect { case (isValid, msg) if !isValid => msg }

      if (invalidMessages.nonEmpty) {
        throw new Exception(
          s"invalid config.\n${invalidMessages.mkString("\n")}"
        )
      }
      Config(
        repositoryFormatVersion = coreNode.getInt("repositoryformatversion", -1)
      )

    } else throw new Exception(s"Config file doesn't exist. path:$confPath")
  }
}
