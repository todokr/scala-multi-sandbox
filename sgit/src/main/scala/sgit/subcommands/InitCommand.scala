package sgit.subcommands

import java.nio.file.Path

import sgit.models.Repository

object InitCommand {

  def run(executingPath: Path): Unit = {
    println("git init!")
    Repository.init(executingPath): Unit
  }
}
