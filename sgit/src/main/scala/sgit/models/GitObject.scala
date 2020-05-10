package sgit.models

import scala.util.chaining._

sealed trait GitObject {
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
case class GitCommit() extends GitObject {
  override def serialize: Array[Byte] = ???
}

case class GitTree() extends GitObject {
  override def serialize: Array[Byte] = ???
}

case class GitTag() extends GitObject {
  override def serialize: Array[Byte] = ???
}

case class GitBlob(blobData: Array[Byte]) extends GitObject {
  override def serialize: Array[Byte] = blobData
}
