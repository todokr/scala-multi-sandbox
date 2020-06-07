package sgit.models

import java.nio.file.{Files, Path}
import java.util.zip.InflaterInputStream

import scala.util.chaining._

import sgit.{CommitParser, TreeParser}

sealed abstract class GitObject(val typeName: String) {
  def serialize: Array[Byte]
}

object GitObject {

  def deserialize(data: Iterator[Byte]): GitObject = {
    val objectType = data.takeWhile(_ != ' ').toArray.pipe(new String(_))
    val content = data.takeWhile(_ != -1).toArray
    data
      .takeWhile(_ != 0x00)
      .toArray
      .pipe(new String(_).toInt) // データサイズは捨てる
    objectType match {
      case "commit" => CommitParser.parse(new String(content))
      case "tree"   => TreeParser.parseOne(content)._1
      case "tag"    => GitTag()
      case "blob"   => GitBlob.of(content)
      case x        => throw new Exception(s"Unknown type $x for object")
    }
  }
}
case class GitCommit(tree: String,
                     parent: Option[String],
                     author: String,
                     committer: String,
                     message: String)
    extends GitObject("commit") {
  override def serialize: Array[Byte] =
    s"""tree $tree
       |parent ${parent.getOrElse("-")}
       |author $author
       |committer $committer
       |
       |$message""".stripMargin.getBytes

  def resolveParent(objectsPath: Path): Option[GitCommit] =
    parent.flatMap { p =>
      val (dir, file) = p.splitAt(2)
      val parentPath = objectsPath.resolve(dir).resolve(file)
      if (!Files.exists(parentPath)) {
        None
      } else {
        val is = new InflaterInputStream(Files.newInputStream(parentPath))
        val data = Iterator.continually(is.read()).map(_.toByte)
        val commit = GitObject.deserialize(data) match {
          case c: GitCommit => Some(c)
          case _            => None
        }
        is.close()
        commit
      }
    }
}

case class GitTree(mode: String, path: String, hash: String)
    extends GitObject("tree") {
  def pretty: String = s"$mode $path $hash"
  override def serialize: Array[Byte] = pretty.getBytes
}

case class GitTag() extends GitObject("tag") {
  override def serialize: Array[Byte] = ???
}

case class GitBlob(blobData: Array[Byte]) extends GitObject("blob") {
  override def serialize: Array[Byte] = blobData
}

object GitBlob {
  def of(data: Array[Byte]): GitBlob = GitBlob(data)
}
