package sgit.models

import scala.util.chaining._

import sgit.CommitParser

sealed abstract class GitObject(val typeName: String) {
  def serialize: Array[Byte]
}

object GitObject {

  def deserialize(data: Iterator[Byte]): GitObject = {
    val objectType = data.takeWhile(_ != ' ').toArray.pipe(new String(_))
    data.takeWhile(_ != 0x00).toArray.pipe(new String(_).toInt) // データサイズは捨てる
    objectType match {
      case "commit" =>
        CommitParser.parse(data.takeWhile(_ != -1).toArray.pipe(new String(_)))
      case "tree" => GitTree()
      case "tag"  => GitTag()
      case "blob" => GitBlob.of(data.takeWhile(_ != -1).toArray)
      case x      => throw new Exception(s"Unknown type $x for object")
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
}

case class Person(name: String, email: String)

case class GitTree() extends GitObject("tree") {
  override def serialize: Array[Byte] = ???
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
