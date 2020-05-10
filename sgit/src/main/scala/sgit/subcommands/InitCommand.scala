package sgit.subcommands

import java.nio.file.Path

import sgit.models.Repository

object InitCommand {

  def run(executingPath: Path): Unit = {
    val repository = Repository.init(executingPath)
    println(s"Initialized empty Git repository in ${repository.workTree}")
  }
}
