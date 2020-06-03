package sgit.subcommands

import java.nio.file.{Files, Path}
import java.util.zip.InflaterInputStream

import scala.util.chaining._
import scala.jdk.CollectionConverters._

import sgit.models.{GitCommit, GitObject}

object LogCommand {

  def run(executingPath: Path): Unit = {
    val gitDir = executingPath.resolve(".git")
    val head = Files.readAllLines(gitDir.resolve("HEAD")).asScala.head
    val ref = head.split(":")(1).trim
    val commit = Files.readAllLines(gitDir.resolve(ref)).asScala.head
    val (dir, file) = commit.splitAt(2)
    val objectsPath = gitDir.resolve("objects")
    val gitObjectPath =
      objectsPath
        .resolve(dir)
        .resolve(file)
        .normalize()
    if (!gitObjectPath.startsWith(executingPath)) {
      throw new Exception("Invalid object")
    } else if (!Files.exists(gitObjectPath)) {
      throw new Exception(s"no such commit. commit=${commit}")
    }
    val is = new InflaterInputStream(Files.newInputStream(gitObjectPath))
    val data = Iterator.continually(is.read()).map(_.toByte)
    val commitObject = GitObject.deserialize(data) match {
      case c: GitCommit => c
      case _            => throw new Exception("not a commit object")
    }

    def _loop(current: GitCommit, acc: List[GitCommit] = Nil): List[GitCommit] =
      current.resolveParent(objectsPath) match {
        case Some(parent) => _loop(parent, parent :: acc)
        case None         => acc
      }
    val serializedCommits =
      _loop(commitObject).map(_.serialize.pipe(new String(_)))

    val pretty = serializedCommits.mkString(s"\n${"-" * 80}\n")
    println(pretty)
  }
}
