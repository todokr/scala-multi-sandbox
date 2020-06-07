package sgit

import java.nio.file.{Path, Paths}
import java.util.Locale

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand, Option, Parameters}
import sgit.models.Repository
import sgit.subcommands.{
  CatFileCommand,
  HashObjectCommand,
  InitCommand,
  LogCommand,
  LsTreeCommand
}

@Command(
  name = "sgit",
  version = Array("0.0.1"),
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[HelpCommand]),
  description = Array("A toy git")
)
class Main extends Runnable {
  val executingPath: Path = Paths.get("").toAbsolutePath

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
    InitCommand.run(workTree)
  }

  @Command(
    name = "cat-file",
    description = Array(
      "Provide content or type and size information for repository objects"
    )
  )
  def catFile(
    @Option(names = Array("-t"), description = Array("Show object type"))
    showType: Boolean,
    @Option(
      names = Array("-p"),
      description = Array("Pretty-print object's content")
    )
    prettyPrint: Boolean,
    @Parameters(index = "0", description = Array("The object to display"))
    hash: String
  ): Unit = {
    CatFileCommand.run(
      showType = showType,
      prettyPrint = prettyPrint,
      hash = hash,
      executingPath = executingPath
    )
  }

  @Command(
    name = "hash-object",
    description =
      Array("Compute object ID and optionally creates a blob from a file")
  )
  def hashObject(
    @Option(
      names = Array("-w"),
      description = Array("Actually write the object into the database")
    )
    isWrite: Boolean,
    @Option(
      names = Array("-t"),
      defaultValue = "blob",
      description = Array("Specify the type")
    )
    objectType: String,
    @Parameters(index = "0", description = Array("Read object from <file>"))
    file: Path
  ): Unit = {
    val repository = Repository.load(executingPath)
    HashObjectCommand.run(isWrite, objectType, file, repository)
  }

  @Command(name = "log", description = Array("Shows the commit logs."))
  def log(): Unit = {
    LogCommand.run(executingPath)
  }

  @Command(
    name = "ls-tree",
    description = Array("List the contents of a tree object")
  )
  def lsTree(
    @Parameters(index = "0", description = Array("Id of a tree-ish"))
    hash: String
  ): Unit = {
    LsTreeCommand.run(executingPath, hash)
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
