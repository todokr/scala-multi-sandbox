package sgit.models

import scala.util.chaining._

sealed abstract class GitObject(val typeName: String) {
  def serialize: Array[Byte]
}

object GitObject {

  def deserialize(data: Iterator[Byte]): GitObject = {
    val objectType = data.takeWhile(_ != ' ').toArray.pipe(new String(_))
    objectType match {
      case "commit" => GitCommit()
      case "tree"   => GitTree()
      case "tag"    => GitTag()
      case "blob"   => GitBlob(data.takeWhile(_ != -1).toArray)
      case x        => throw new Exception(s"Unknown type $x for object")
    }
  }
}
case class GitCommit() extends GitObject("commit") {
  override def serialize: Array[Byte] = ???
}

case class GitTree() extends GitObject("tree") {
  override def serialize: Array[Byte] = ???
}

case class GitTag() extends GitObject("tag") {
  override def serialize: Array[Byte] = ???
}

case class GitBlob(blobData: Array[Byte]) extends GitObject("blob") {
  override def serialize: Array[Byte] = blobData
}
