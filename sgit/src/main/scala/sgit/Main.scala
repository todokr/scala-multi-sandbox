package sgit

import java.nio.file.{Path, Paths}
import java.util.Locale

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand, Parameters}
import sgit.subcommands.InitCommand

@Command(
  name = "sgit",
  version = Array("0.0.1"),
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[HelpCommand]),
  description = Array("A toy git")
)
class Main extends Runnable {
  val executingPath: Path = Paths.get(".").toAbsolutePath

  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  @Command(
    name = "init",
    description =
      Array("Create an empty Git repository or reinitialize an existing one")
  )
  def init(
    @Parameters(index = "0", defaultValue = ".") targetDir: Path
  ): Unit = {
    val workTree = executingPath.resolve(targetDir).normalize()
    println(workTree)
    InitCommand.run(workTree)
  }

  @Command(
    name = "language",
    description =
      Array("Resolve ISO language code (ISO 639-1 or -2, two/three letters)")
  )
  def language(
    @Parameters(
      arity = "1..*n",
      paramLabel = "<language code 1> <language code 2>",
      description = Array("language code(s) to be resolved")
    )
    languageCodes: Array[String]
  ): Unit = {
    for (code <- languageCodes) {
      println(
        s"${code.toUpperCase()}: ".concat(new Locale(code).getDisplayLanguage)
      )
    }
  }

  def run(): Unit = {
    throw new CommandLine.ParameterException(
      spec.commandLine(),
      "Specify a subcommand"
    )
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    // System.exit()
    new CommandLine(new Main()).execute(args: _*)
    ()
  }
}
