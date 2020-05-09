package sgit.models

import java.nio.file.{Files, Path}

import sgit.ConfigParser

object Repository {

  /**
    * Load repository from existing Git directory
    */
  def load(workTree: Path): Repository = {
    if (!Files.isDirectory(workTree)) {
      throw new Exception("Not a Git repository")
    }
    val gitDir = workTree.resolve(".git")
    val confPath = workTree.resolve("config")
    val config = ConfigParser.parse(confPath)
    if (!config.isSupportedVersion) {
      throw new Exception(
        s"Unsupported repositoryformatversion. Only supports version 0. version:${config.isSupportedVersion}"
      )
    }
    Repository(workTree, gitDir, config)
  }

  /**
    * Initialize directory as empty Git repository
    */
  def init(workTree: Path): Repository = {
    if (!Files.isDirectory(workTree)) {
      throw new Exception(s"Not a directory. path: $workTree")
    }
    val gitDir = workTree.resolve(".git")
    if (Option(gitDir.toFile.list()).exists(_.nonEmpty)) {
      throw new Exception(s"Non empty Git repository. path: $workTree")
    }
    Files.createDirectory(gitDir)
    val config = Config.init(gitDir)
    Branches.init(gitDir)
    Objects.init(gitDir)
    Refs.init(gitDir)

    Repository(workTree, gitDir, config)
  }
}

case class Repository(workTree: Path, gitDir: Path, config: Config)
